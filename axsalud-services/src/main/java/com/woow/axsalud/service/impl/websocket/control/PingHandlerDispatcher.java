package com.woow.axsalud.service.impl.websocket.control;

import com.woow.axsalud.data.repository.ConsultationSessionRepository;
import com.woow.axsalud.service.api.messages.control.ControlMessage;
import com.woow.axsalud.service.api.messages.control.ControlMessageDTO;
import com.woow.axsalud.service.api.messages.control.ControlMessageType;
import com.woow.axsalud.service.api.websocket.ControlMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class PingHandlerDispatcher implements ControlMessageHandler {


    private SimpMessagingTemplate messagingTemplate;
    private ConsultationSessionRepository consultationSessionRepository;

    public PingHandlerDispatcher(final SimpMessagingTemplate messagingTemplate,
                             final ConsultationSessionRepository consultationSessionRepository) {

        this.messagingTemplate = messagingTemplate;
        this.consultationSessionRepository = consultationSessionRepository;
    }

    @Override
    public boolean supports(ControlMessageType type) {
        return ControlMessageType.PING.equals(type);
    }

    @Override
    public void handledControlMessage(ControlMessage message) {
        log.info("ping message received: {}", message);
        ControlMessageDTO controlMessageDTO = new ControlMessageDTO();
        controlMessageDTO.setMessageType(ControlMessageType.PONG);
        controlMessageDTO.setTimeProcessed(LocalDateTime.now());
        controlMessageDTO.setSender(message.getUserName());

        String controlCommunicationTopic = "/topic/consultation." + message.getConsultationId() +
                ".session." + message.getConsultationSessionId() + ".control";
        messagingTemplate.convertAndSend(controlCommunicationTopic, controlMessageDTO);
        log.info("PONG message send: {}", controlMessageDTO);
    }

}
