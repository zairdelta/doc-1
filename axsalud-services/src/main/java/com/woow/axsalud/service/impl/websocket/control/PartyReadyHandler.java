package com.woow.axsalud.service.impl.websocket.control;

import com.woow.axsalud.common.AXSaludUserRoles;
import com.woow.axsalud.data.consultation.ConsultationSession;
import com.woow.axsalud.data.consultation.ConsultationSessionStatus;
import com.woow.axsalud.data.consultation.PartyConsultationStatus;
import com.woow.axsalud.data.repository.ConsultationSessionRepository;
import com.woow.axsalud.service.api.messages.control.ControlMessage;
import com.woow.axsalud.service.api.messages.control.ControlMessageDTO;
import com.woow.axsalud.service.api.messages.control.ControlMessageType;
import com.woow.axsalud.service.api.websocket.ControlMessageHandler;
import com.woow.axsalud.service.impl.websocket.AppOutboundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
public class PartyReadyHandler implements ControlMessageHandler {


    private SimpMessagingTemplate messagingTemplate;
    private ConsultationSessionRepository consultationSessionRepository;
    private AppOutboundService appOutboundService;

    public PartyReadyHandler(final SimpMessagingTemplate messagingTemplate,
                             final ConsultationSessionRepository consultationSessionRepository,
                             final AppOutboundService appOutboundService) {

        this.messagingTemplate = messagingTemplate;
        this.consultationSessionRepository = consultationSessionRepository;
        this.appOutboundService = appOutboundService;
    }

    @Override
    public boolean supports(ControlMessageType type) {
        return ControlMessageType.PARTY_READY.equals(type);
    }

    @Override
    @Transactional
    @Retryable(
            value = {OptimisticLockingFailureException.class, Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void handledControlMessage(final ControlMessage message) {
        log.info("{}_ Processing PARTY_READY message: {}, reading DB with lock ", message.getSessionId(), message);
        if(message.getControlMessageDTO().getMessageType() == ControlMessageType.PARTY_READY) {
            ConsultationSession consultationSession = consultationSessionRepository
                    .findWithLock(UUID.fromString(message.getConsultationSessionId()));
            if(consultationSession.getStatus() == ConsultationSessionStatus.CONFIRMING_PARTIES) {
                log.info("{}_ running hand check process", message.getSessionId());
                if (message.getRoles().contains(AXSaludUserRoles.DOCTOR.getRole())) {
                    consultationSession.setDoctorStatus(PartyConsultationStatus.READY);
                    log.info("{}_ setting Doctor to READY", message.getSessionId());
                } else if (message.getRoles().contains(AXSaludUserRoles.USER.getRole())) {
                    consultationSession.setPatientStatus(PartyConsultationStatus.READY);
                    log.info("{}_ setting Patient to READY", message.getSessionId());
                }

                if(consultationSession.getDoctorStatus() == PartyConsultationStatus.READY &&
                        consultationSession.getPatientStatus() == PartyConsultationStatus.READY) {
                    consultationSession.setStatus(ConsultationSessionStatus.CONNECTING);
                    log.info("{}_ Both parties are ready, sending CHAT_READY event, sessionID: {}",
                            message.getSessionId(), message.getConsultationSessionId());
                    ControlMessageDTO controlMessageDTO = new ControlMessageDTO();
                    controlMessageDTO.setMessageType(ControlMessageType.CHAT_READY);
                    controlMessageDTO.setTimeProcessed(LocalDateTime.now());
                    controlMessageDTO.setDoctor(consultationSession.getDoctor().getCoreUser().getUserName());
                    controlMessageDTO.setPatient(consultationSession.getConsultation()
                            .getPatient().getCoreUser().getUserName());

                    /*String controlCommunicationTopic = "/topic/consultation." + consultationSession.getConsultation().getConsultationId() +
                            ".session." + consultationSession.getConsultationSessionId() + ".control";
                    messagingTemplate.convertAndSend(controlCommunicationTopic, controlMessageDTO);*/
                    appOutboundService.sendConsultationControlEvent(consultationSession.getConsultation().getConsultationId().toString(),
                            consultationSession.getConsultationSessionId().toString(), controlMessageDTO);
                }
                consultationSessionRepository.save(consultationSession);
                log.info("{}_ ConsultationSession updated", message.getSessionId());
            } else {
                log.info("{}_ Getting invalid PARTY Ready message while session is in state: {}"
                        , message.getSessionId(), consultationSession.getStatus());
            }
        }
    }
}
