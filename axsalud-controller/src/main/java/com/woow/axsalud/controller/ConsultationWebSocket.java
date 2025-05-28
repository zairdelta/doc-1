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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;


import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

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

    @MessageMapping("/ping")
    public void gettingPingMessageFromClient(
            Principal principal,
            @Payload ControlMessageDTO controlMessageDTO
    ) {
        log.debug("getting ping frame from user: {}", principal.getName());
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
        log.debug("handledControl webSocket consultationId: {}, consultationSessionId: {}, principal's userName: {}",
                consultationId, consultationSessionId, principal.getName());
        List<String> roles = new ArrayList<>();

        if (principal instanceof UsernamePasswordAuthenticationToken authentication) {
            roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
        } else  if(principal instanceof WebSocketUserPrincipal webSocketUserPrincipal) {
            webSocketUserPrincipal = (WebSocketUserPrincipal) principal;
            roles = webSocketUserPrincipal.getRoles();
        }

        log.debug("Getting control message,: {}, user: {}, roles: {}", controlMessageDTO,
                principal.getName(), roles);
        ControlMessage controlMessage = new ControlMessage();
        controlMessage.setConsultationId(consultationId);
        controlMessage.setConsultationSessionId(consultationSessionId);
        controlMessage.setControlMessageDTO(controlMessageDTO);
        controlMessage.setRoles(roles);
        controlMessage.setUserName(principal.getName());
        controlMessageDispatcher.dispatch(controlMessage);
    }
}
