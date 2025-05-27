package com.woow.axsalud.service.impl.websocket.control;

import com.woow.axsalud.common.AXSaludUserRoles;
import com.woow.axsalud.data.consultation.ConsultationSession;
import com.woow.axsalud.data.consultation.ConsultationSessionStatus;
import com.woow.axsalud.data.consultation.PartyConsultationStatus;
import com.woow.axsalud.data.repository.*;
import com.woow.axsalud.service.api.messages.control.ControlMessage;
import com.woow.axsalud.service.api.messages.control.ControlMessageDTO;
import com.woow.axsalud.service.api.messages.control.ControlMessageType;
import com.woow.axsalud.service.api.websocket.ControlMessageHandler;
import com.woow.core.data.repository.WoowUserRepository;
import com.woow.storage.api.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
public class PartyReadyHandler implements ControlMessageHandler {


    private SimpMessagingTemplate messagingTemplate;
    private ConsultationSessionRepository consultationSessionRepository;

    public PartyReadyHandler(final SimpMessagingTemplate messagingTemplate,
                             final ConsultationSessionRepository consultationSessionRepository) {

        this.messagingTemplate = messagingTemplate;
        this.consultationSessionRepository = consultationSessionRepository;
    }

    @Override
    public boolean supports(ControlMessageType type) {
        return ControlMessageType.PARTY_READY.equals(type);
    }

    @Override
    public void handledControlMessage(final ControlMessage message) {
        log.info("Processing PARTY_READY message: {} ", message);
        if(message.getControlMessageDTO().getMessageType() == ControlMessageType.PARTY_READY) {
            ConsultationSession consultationSession = consultationSessionRepository
                    .findByConsultationSessionId(UUID.fromString(message.getConsultationSessionId()));
            if(consultationSession.getStatus() == ConsultationSessionStatus.CONFIRMING_PARTIES) {
                log.info("running hand check process");
                if (message.getRoles().contains(AXSaludUserRoles.DOCTOR.getRole())) {
                    consultationSession.setDoctorStatus(PartyConsultationStatus.READY);
                    log.info("setting Doctor to READY");
                } else if (message.getRoles().contains(AXSaludUserRoles.USER.getRole())) {
                    consultationSession.setPatientStatus(PartyConsultationStatus.READY);
                    log.info("setting Patient to READY");
                }

                if(consultationSession.getDoctorStatus() == PartyConsultationStatus.READY &&
                        consultationSession.getPatientStatus() == PartyConsultationStatus.READY) {
                    consultationSession.setStatus(ConsultationSessionStatus.CONNECTING);
                    log.info("Both parties are ready, sending CHAT_READY event, sessionID: {}", message.getConsultationSessionId());
                    ControlMessageDTO controlMessageDTO = new ControlMessageDTO();
                    controlMessageDTO.setMessageType(ControlMessageType.CHAT_READY);
                    controlMessageDTO.setTimeProcessed(LocalDateTime.now());
                    controlMessageDTO.setDoctor(consultationSession.getDoctor().getCoreUser().getUserName());
                    controlMessageDTO.setPatient(consultationSession.getConsultation()
                            .getPatient().getCoreUser().getUserName());

                    String controlCommunicationTopic = "/topic/consultation." + consultationSession.getConsultation().getConsultationId() +
                            ".session." + consultationSession.getConsultationSessionId() + ".control";
                    messagingTemplate.convertAndSend(controlCommunicationTopic, controlMessageDTO);
                }
                consultationSessionRepository.save(consultationSession);
                log.info("ConsultationSession updated");
            }
        }
    }
}
