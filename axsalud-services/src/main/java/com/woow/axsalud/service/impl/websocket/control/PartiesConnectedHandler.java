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
@Transactional
public class PartiesConnectedHandler implements ControlMessageHandler {


    private SimpMessagingTemplate messagingTemplate;
    private ConsultationSessionRepository consultationSessionRepository;
    private AppOutboundService appOutboundService;

    public PartiesConnectedHandler(final SimpMessagingTemplate messagingTemplate,
                                   final ConsultationSessionRepository consultationSessionRepository,
                                   final AppOutboundService appOutboundService) {

        this.messagingTemplate = messagingTemplate;
        this.consultationSessionRepository = consultationSessionRepository;
        this.appOutboundService = appOutboundService;
    }

    @Override
    public boolean supports(ControlMessageType type) {
        return ControlMessageType.PARTY_CONNECTED.equals(type);
    }

    @Override
    @Transactional
    @Retryable(
            value = {OptimisticLockingFailureException.class, Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void handledControlMessage(final ControlMessage message) {
        log.info("Processing PARTY_CONNECTED message: {}, reading DB with lock ", message);
        if(message.getControlMessageDTO().getMessageType() == ControlMessageType.PARTY_CONNECTED) {
            ConsultationSession consultationSession = consultationSessionRepository
                    .findWithLock(UUID.fromString(message.getConsultationSessionId()));
            if(consultationSession.getStatus() == ConsultationSessionStatus.CONNECTING) {
                log.info("running connected process");
                if (message.getRoles().contains(AXSaludUserRoles.DOCTOR.getRole())) {
                    consultationSession.setDoctorStatus(PartyConsultationStatus.CONNECTED);
                    log.info("setting Doctor to CONNECTED");
                } else if (message.getRoles().contains(AXSaludUserRoles.USER.getRole())) {
                    consultationSession.setPatientStatus(PartyConsultationStatus.CONNECTED);
                    log.info("setting Patient to CONNECTED");
                }

                if(consultationSession.getDoctorStatus() == PartyConsultationStatus.CONNECTED &&
                        consultationSession.getPatientStatus() == PartyConsultationStatus.CONNECTED) {
                    consultationSession.setStatus(ConsultationSessionStatus.CONNECTED);
                    log.info("Both parties are CONNECTED, sending CONNECTED event, sessionID: {}", message.getConsultationSessionId());
                    ControlMessageDTO controlMessageDTO = new ControlMessageDTO();
                    controlMessageDTO.setMessageType(ControlMessageType.CONNECTED);
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
                log.info("ConsultationSession updated");
            } else {
                log.info("Receiving PARTY_CONNECTED where consultationSession is not in CONNECTING state");
            }
        }
    }
}
