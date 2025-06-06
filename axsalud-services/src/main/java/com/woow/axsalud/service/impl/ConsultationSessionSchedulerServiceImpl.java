package com.woow.axsalud.service.impl;

import com.woow.axsalud.common.AXSaludUserRoles;
import com.woow.axsalud.data.consultation.ConsultationSession;
import com.woow.axsalud.data.consultation.ConsultationSessionStatus;
import com.woow.axsalud.data.consultation.PartyConsultationStatus;
import com.woow.axsalud.data.repository.AxSaludUserRepository;
import com.woow.axsalud.data.repository.ConsultationMessageRepository;
import com.woow.axsalud.data.repository.ConsultationSessionRepository;
import com.woow.axsalud.service.api.ConsultationSessionSchedulerService;
import com.woow.axsalud.service.api.dto.ConsultationMessgeTypeEnum;
import com.woow.axsalud.service.api.messages.ConsultationEventDTO;
import com.woow.axsalud.service.api.messages.control.SessionAbandonedDTO;
import com.woow.core.data.repository.WoowUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ConsultationSessionSchedulerServiceImpl implements ConsultationSessionSchedulerService {

    @Value("${ woow.app.telemedicine.consultation.session.connected.idleTimeoutInSeconds:60}")
    private int CONNECTED_SESSION_IDLE_IN_SECONDS;
    private WoowUserRepository woowUserRepository;
    private AxSaludUserRepository axSaludUserRepository;
    private ConsultationMessageRepository consultationMessageRepository;
    private ModelMapper modelMapper;
    private SimpMessagingTemplate messagingTemplate;
    private ConsultationSessionRepository consultationSessionRepository;

    public ConsultationSessionSchedulerServiceImpl(
                                   WoowUserRepository woowUserRepository,
                                   AxSaludUserRepository axSaludUserRepository,
                                   ModelMapper modelMapper,
                                   SimpMessagingTemplate messagingTemplate,
                                   ConsultationMessageRepository consultationMessageRepository,
                                   final ConsultationSessionRepository consultationSessionRepository) {
        this.woowUserRepository = woowUserRepository;
        this.axSaludUserRepository = axSaludUserRepository;
        this.modelMapper = modelMapper;
        this.consultationMessageRepository = consultationMessageRepository;
        this.messagingTemplate = messagingTemplate;
        this.consultationSessionRepository = consultationSessionRepository;
    }

    @Scheduled(fixedRate = 30000)
    @Override
    public void sendPing() {
        log.info("sending ping message to topic/doctor-events: {}", Map.of("messageType", "ping"));
        messagingTemplate.convertAndSend("/topic/doctor-events", Map.of("messageType", "ping"));
    }

    @Scheduled(fixedRate = 60000)
    @Override
    @Transactional
    public void sendSessionTerminated() {
        log.info("Running SessionTerminated finding abandoned session");
        LocalDateTime localDateTime = LocalDateTime.now();
        localDateTime = localDateTime.minusSeconds(CONNECTED_SESSION_IDLE_IN_SECONDS);

        List<ConsultationSessionStatus> statuses = List.of(ConsultationSessionStatus.CONNECTED,
                ConsultationSessionStatus.CONNECTING);

        List<ConsultationSession> consultationSessionsDoctorLost =
                consultationSessionRepository.findByDoctorLastTimeSeen(localDateTime, statuses);
        log.info("Sessions list size found to terminated for doctors: {}", consultationSessionsDoctorLost.size());

        consultationSessionsDoctorLost.stream()
                .map(session -> handledSessionAbandoned(session, AXSaludUserRoles.DOCTOR))
                .forEach(consultationEventDTO -> sendConsultationEvent(consultationEventDTO));

        List<ConsultationSession> consultationSessionsPatientLost =
                consultationSessionRepository.findByPatientLastTimeSeen(localDateTime, statuses);
        log.info("Sessions list size found to terminated for Patient: {}", consultationSessionsDoctorLost.size());

        consultationSessionsPatientLost.stream()
                .map(session -> handledSessionAbandoned(session, AXSaludUserRoles.USER))
                .forEach(consultationEventDTO -> sendConsultationEvent(consultationEventDTO));
    }

    @Transactional
    private ConsultationEventDTO<SessionAbandonedDTO>
    handledSessionAbandoned(final ConsultationSession consultationSession,
                                                         final AXSaludUserRoles role) {

        log.info("SessionAbandoned, sessionID: {}, role: {}, doctor: {}, patient: {}", consultationSession.getConsultationSessionId(),
                 role, consultationSession.getDoctor().getCoreUser().getUserName(),
                consultationSession.getConsultation().getPatient().getCoreUser().getUserName());

        SessionAbandonedDTO sessionAbandonedDTO = new SessionAbandonedDTO();
        sessionAbandonedDTO.setNewConsultationSessionStatus(ConsultationSessionStatus.ABANDONED.getStatus());
        sessionAbandonedDTO.setConsultationSessionId(consultationSession.getConsultationSessionId().toString());
        sessionAbandonedDTO.setConsultationId(consultationSession.getConsultation().getConsultationId().toString());

        if(role == AXSaludUserRoles.DOCTOR) {
            consultationSession.setDoctorStatus(PartyConsultationStatus.DROPPED);
            sessionAbandonedDTO.setRole(AXSaludUserRoles.DOCTOR.getRole());
            sessionAbandonedDTO.setUserName(consultationSession.getDoctor()
                    .getCoreUser().getUserName());
            sessionAbandonedDTO.setLastTimeSeen(consultationSession.getDoctorLastTimePing());
        } else {
            consultationSession.setPatientStatus(PartyConsultationStatus.DROPPED);
            sessionAbandonedDTO.setRole(AXSaludUserRoles.USER.getRole());
            sessionAbandonedDTO.setUserName(consultationSession.getConsultation()
                    .getPatient().getCoreUser().getUserName());
            sessionAbandonedDTO.setLastTimeSeen(consultationSession.getPatientLastTimePing());
        }
        ConsultationEventDTO<SessionAbandonedDTO> consultationEventDTO = new ConsultationEventDTO<>();
        consultationEventDTO.setTimeProcessed(LocalDateTime.now());
        consultationEventDTO.setPayload(sessionAbandonedDTO);
        consultationEventDTO.setId(-1);
        consultationEventDTO.setMessageType(ConsultationMessgeTypeEnum.SESSION_ABANDONED);
        log.info("Session abandoned event: {}", sessionAbandonedDTO);

        consultationSession.setStatus(ConsultationSessionStatus.ABANDONED);
        consultationSessionRepository.save(consultationSession);

        return consultationEventDTO;
    }

    private void sendConsultationEvent(ConsultationEventDTO<SessionAbandonedDTO> consultationEventDTO) {
        SessionAbandonedDTO sessionAbandonedDTO = consultationEventDTO.getPayload();
        String controlCommunicationTopic = "/topic/consultation." + sessionAbandonedDTO.getConsultationId() +
                ".session." + sessionAbandonedDTO.getConsultationSessionId() + ".control";
        messagingTemplate.convertAndSend(controlCommunicationTopic, consultationEventDTO);
    }

}
