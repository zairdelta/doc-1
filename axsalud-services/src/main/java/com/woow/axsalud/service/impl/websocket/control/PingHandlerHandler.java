package com.woow.axsalud.service.impl.websocket.control;

import com.woow.axsalud.common.AXSaludUserRoles;

import com.woow.axsalud.data.repository.ConsultationSessionRepository;
import com.woow.axsalud.service.api.messages.control.ControlMessage;
import com.woow.axsalud.service.api.messages.control.ControlMessageDTO;
import com.woow.axsalud.service.api.messages.control.ControlMessageType;
import com.woow.axsalud.service.api.websocket.ControlMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
public class PingHandlerHandler implements ControlMessageHandler {


    private SimpMessagingTemplate messagingTemplate;
    private ConsultationSessionRepository consultationSessionRepository;

    public PingHandlerHandler(final SimpMessagingTemplate messagingTemplate,
                              final ConsultationSessionRepository consultationSessionRepository) {

        this.messagingTemplate = messagingTemplate;
        this.consultationSessionRepository = consultationSessionRepository;
    }

    @Override
    public boolean supports(ControlMessageType type) {
        return ControlMessageType.PING.equals(type);
    }

    @Override
    @Transactional
    public void handledControlMessage(ControlMessage message) {
        log.info("ping message received: {}", message);
        ControlMessageDTO controlMessageDTO = new ControlMessageDTO();
        controlMessageDTO.setMessageType(ControlMessageType.PONG);
        controlMessageDTO.setTimeProcessed(LocalDateTime.now());
        controlMessageDTO.setSender(message.getUserName());
        UUID sessionId = UUID.fromString(message.getConsultationSessionId());
        int numberOfRowsAffected = 0 ;
        if(message.getRoles().contains(AXSaludUserRoles.DOCTOR.getRole())) {
            log.info("updating Doctor lastPing, not intrusive query");
            numberOfRowsAffected = consultationSessionRepository.updateDoctorLastPing(sessionId, LocalDateTime.now());

        } else {
            log.info("updating Patient lastPing, not intrusive query");
            numberOfRowsAffected = consultationSessionRepository.updatePatientLastPing(sessionId, LocalDateTime.now());
        }

        log.info("number of rows affected by the update: {}", numberOfRowsAffected);

        String controlCommunicationTopic = "/topic/consultation." + message.getConsultationId() +
                ".session." + message.getConsultationSessionId() + ".control";
        messagingTemplate.convertAndSend(controlCommunicationTopic, controlMessageDTO);
        log.info("PONG message send: {}", controlMessageDTO);
    }

}
