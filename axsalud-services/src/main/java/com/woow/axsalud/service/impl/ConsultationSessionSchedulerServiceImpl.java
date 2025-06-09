package com.woow.axsalud.service.impl;

import com.woow.axsalud.common.AXSaludUserRoles;
import com.woow.axsalud.data.consultation.ConsultationSession;
import com.woow.axsalud.data.consultation.ConsultationSessionStatus;
import com.woow.axsalud.data.consultation.ConsultationStatus;
import com.woow.axsalud.data.repository.AxSaludUserRepository;
import com.woow.axsalud.data.repository.ConsultationMessageRepository;
import com.woow.axsalud.data.repository.ConsultationSessionRepository;
import com.woow.axsalud.service.api.ConsultationService;
import com.woow.axsalud.service.api.ConsultationSessionSchedulerService;
import com.woow.axsalud.service.impl.websocket.AppOutboundService;
import com.woow.core.data.repository.WoowUserRepository;
import com.woow.security.api.ws.PlatformService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.woow.axsalud.common.WoowConstants.NO_TRANSPORT_SESSION;

@Service
@Slf4j
public class ConsultationSessionSchedulerServiceImpl implements ConsultationSessionSchedulerService {

    @Value("${ woow.app.telemedicine.consultation.session.connected.idleTimeoutInSeconds:120}")
    private int CONNECTED_SESSION_IDLE_IN_SECONDS;

    private WoowUserRepository woowUserRepository;
    private AxSaludUserRepository axSaludUserRepository;
    private ConsultationMessageRepository consultationMessageRepository;
    private AppOutboundService appOutboundService;
    private SimpMessagingTemplate messagingTemplate;
    private ConsultationSessionRepository consultationSessionRepository;
    private PlatformService platformService;

    private ConsultationService consultationService;

    public ConsultationSessionSchedulerServiceImpl(
                                   final PlatformService platformService,
                                   WoowUserRepository woowUserRepository,
                                   AxSaludUserRepository axSaludUserRepository,
                                   final AppOutboundService appOutboundService,
                                   SimpMessagingTemplate messagingTemplate,
                                   final ConsultationService consultationService,
                                   ConsultationMessageRepository consultationMessageRepository,
                                   final ConsultationSessionRepository consultationSessionRepository) {
        this.woowUserRepository = woowUserRepository;
        this.axSaludUserRepository = axSaludUserRepository;
        this.consultationMessageRepository = consultationMessageRepository;
        this.messagingTemplate = messagingTemplate;
        this.consultationSessionRepository = consultationSessionRepository;
        this.platformService = platformService;
        this.appOutboundService = appOutboundService;
        this.consultationService = consultationService;
    }

    @Scheduled(fixedRate = 30000)
    @Override
    public void sendPing() {
        log.info("sending ping message to topic/doctor-events: {}", Map.of("messageType", "ping"));
        messagingTemplate.convertAndSend("/topic/doctor-events", Map.of("messageType", "ping"));
    }

   // @Scheduled(fixedRate = 60000)
    @Override
    @Transactional
    public void sendSessionTerminated() {
        log.info("Running SessionTerminated finding abandoned session");
        LocalDateTime localDateTime = LocalDateTime.now();
        localDateTime = localDateTime.minusSeconds(CONNECTED_SESSION_IDLE_IN_SECONDS);

        List<ConsultationSessionStatus> statuses = List.of(ConsultationSessionStatus.CONNECTED,
                ConsultationSessionStatus.CONNECTING, ConsultationSessionStatus.CONFIRMING_PARTIES);

        List<ConsultationSession> consultationSessionsDoctorLost =
                consultationSessionRepository.findByDoctorLastTimeSeen(localDateTime, statuses);
        log.info("Sessions list size found to terminated for doctors: {}", consultationSessionsDoctorLost.size());

        consultationSessionsDoctorLost.stream()
                .map(session -> consultationService.handledSessionAbandoned(NO_TRANSPORT_SESSION, session,
                        ConsultationSessionStatus.ABANDONED_EXPIRED, ConsultationStatus.ABANDONED, AXSaludUserRoles.DOCTOR,
                        session.getDoctor().getCoreUser().getUserName()))
                .map(event->consultationService.sendConsultationEvent(NO_TRANSPORT_SESSION, event))
                .forEach(platformService::appSessionTerminated);

        List<ConsultationSession> consultationSessionsPatientLost =
                consultationSessionRepository.findByPatientLastTimeSeen(localDateTime, statuses);
        log.info("Sessions list size found to terminated for Patient: {}", consultationSessionsPatientLost.size());

        consultationSessionsPatientLost.stream()
                .map(session -> consultationService.handledSessionAbandoned(NO_TRANSPORT_SESSION, session,
                        ConsultationSessionStatus.ABANDONED_EXPIRED, ConsultationStatus.ABANDONED, AXSaludUserRoles.USER,
                        session.getConsultation().getPatient().getCoreUser().getUserName()))
                .map(event -> consultationService.sendConsultationEvent(NO_TRANSPORT_SESSION, event))
                .forEach(platformService::appSessionTerminated);
    }

}
