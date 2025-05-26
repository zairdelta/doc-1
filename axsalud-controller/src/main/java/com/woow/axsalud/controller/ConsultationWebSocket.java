package com.woow.axsalud.controller;

import com.woow.axsalud.service.api.ConsultationService;
import com.woow.axsalud.service.api.messages.ConsultationMessageDTO;
import com.woow.axsalud.service.api.exception.ConsultationServiceException;
import com.woow.axsalud.service.api.messages.control.ControlMessage;
import com.woow.axsalud.service.api.messages.control.ControlMessageDTO;
import com.woow.axsalud.service.impl.websocket.control.ControlMessageDispatcher;
import com.woow.security.api.WebSocketUserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
public class ConsultationWebSocket {

    private ConsultationService consultationService;
    private ControlMessageDispatcher controlMessageDispatcher;


    public ConsultationWebSocket(ConsultationService consultationService,
                                 ControlMessageDispatcher controlMessageDispatcher) {
        this.consultationService = consultationService;
        this.controlMessageDispatcher = controlMessageDispatcher;
    }

    @MessageMapping("/consultation/{consultationId}/session/{consultationSessionId}/private")
    public void sendPrivateMessage(
            @DestinationVariable String consultationId,
            @DestinationVariable String consultationSessionId,
            @Payload ConsultationMessageDTO consultationMessage,
            Principal principal
    ) {
        consultationMessage.setConsultationId(consultationId);
        consultationMessage.setConsultationSessionId(consultationSessionId);
        consultationMessage.setSender(principal.getName());
        log.info("consultationMessage Received: {}", consultationMessage);
        consultationService.handledConsultationMessage(consultationMessage);
    }

    // I need to handle this message in the control topic
    @MessageMapping("/consultation/{consultationId}/session/{consultationSessionId}/close")
    public void closeSession(
            @DestinationVariable String consultationId,
            @DestinationVariable String consultationSessionId,
            Principal principal
    ) {
        log.info("closing consultationId: {}, consultationSessionId: {}, Sent By: {}",
                consultationId,
                consultationSessionId, principal.getName());
        try {
            consultationService.closeSession(consultationId,
                    consultationSessionId, principal.getName());
        } catch (ConsultationServiceException e) {
            throw new RuntimeException(e);
        }
    }

    /**
         Patient starts a new consultation, patient subscribe to topic:
        topic/consultation/{consultationId}/session/{sessionId}/control
     2.- patient waits to get an event Doctor assigned
     3.- Patient send PARTY_READY to server from clients to backend:
         /app/consultation/{consultationId}/session/{sessionId}/control
     4.- DOCTOR send PARTY_READY to server
        /app/consultation/{consultationId}/session/{sessionId}/control
     5.- both patient and doctor create subscriptions to /user/queue/messages
     6.- Server send CHAT_READY to patient and Doctor, chat can start

        /topic/consultation/{consultationId}/session/{sessionId}/control → from backend to clients (broadcast)
     **/

    @MessageMapping("/consultation/{consultationId}/session/{consultationSessionId}/control")
    public void handleControl(@DestinationVariable String consultationId,
                              @DestinationVariable String consultationSessionId,
                              @Payload ControlMessageDTO controlMessageDTO,
                              Principal principal) {
        log.debug("handledControl webSocket consultationId: {}, consultationSessionId: {}",
                consultationId, consultationSessionId);
        WebSocketUserPrincipal webSocketUserPrincipal = null;
        if(principal instanceof WebSocketUserPrincipal) {
            webSocketUserPrincipal = (WebSocketUserPrincipal) principal;
        }

        if(webSocketUserPrincipal != null) {

            log.debug("Getting controll message,: {}, user: {}", controlMessageDTO,
                    webSocketUserPrincipal.getName());
            ControlMessage controlMessage = new ControlMessage();
            controlMessage.setConsultationId(consultationId);
            controlMessage.setConsultationSessionId(consultationSessionId);
            controlMessage.setControlMessageDTO(controlMessageDTO);
            controlMessage.setRoles(webSocketUserPrincipal.getRoles());
            controlMessage.setUserName(webSocketUserPrincipal.getName());
            controlMessageDispatcher.dispatch(controlMessage);

        } else {
            log.error("UserPrincipal is not an instance of WebSocketUserPrincipal: {} ", principal);
        }

    }
}
