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
import com.woow.security.api.WooWRoleType;
import com.woow.storage.api.StorageService;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

public class PartyReadyHandler implements ControlMessageHandler {

    private ConsultationRepository consultationRepository;
    private WoowUserRepository woowUserRepository;
    private AxSaludUserRepository axSaludUserRepository;
    private ConsultationMessageRepository consultationMessageRepository;
    private ModelMapper modelMapper;
    private SimpMessagingTemplate messagingTemplate;
    private StorageService storageService;
    private ConsultationDocumentRepository consultationDocumentRepository;
    private ConsultationSessionRepository consultationSessionRepository;

    public PartyReadyHandler(ConsultationRepository consultationRepository,
                             WoowUserRepository woowUserRepository,
                             AxSaludUserRepository axSaludUserRepository,
                             ModelMapper modelMapper,
                             SimpMessagingTemplate messagingTemplate,
                             ConsultationMessageRepository consultationMessageRepository,
                             final ConsultationDocumentRepository consultationDocumentRepository,
                             final StorageService storageService,
                             final ConsultationSessionRepository consultationSessionRepository) {
        this.consultationRepository = consultationRepository;
        this.woowUserRepository = woowUserRepository;
        this.axSaludUserRepository = axSaludUserRepository;
        this.modelMapper = modelMapper;
        this.consultationMessageRepository = consultationMessageRepository;
        this.messagingTemplate = messagingTemplate;
        this.consultationDocumentRepository = consultationDocumentRepository;
        this.storageService = storageService;
        this.consultationSessionRepository = consultationSessionRepository;
    }

    @Override
    public boolean supports(ControlMessageType type) {
        return ControlMessageType.PARTY_READY.equals(type);
    }

    @Override
    public void handledControlMessage(final ControlMessage message) {
        if(message.getControlMessageDTO().getMessageType() == ControlMessageType.PARTY_READY) {
            ConsultationSession consultationSession = consultationSessionRepository
                    .findByConsultationSessionId(UUID.fromString(message.getConsultationSessionId()));

            if(consultationSession.getStatus() == ConsultationSessionStatus.CONFIRMING_PARTIES) {
                if (message.getRoles().contains(AXSaludUserRoles.DOCTOR)) {
                    consultationSession.setDoctorStatus(PartyConsultationStatus.READY);
                } else if (message.getRoles().contains(AXSaludUserRoles.USER)) {
                    consultationSession.setPatientStatus(PartyConsultationStatus.READY);
                }

                if(consultationSession.getDoctorStatus() == PartyConsultationStatus.READY &&
                    consultationSession.getPatientStatus() == PartyConsultationStatus.READY) {
                    consultationSession.setStatus(ConsultationSessionStatus.CONNECTING);
                    consultationSessionRepository.save(consultationSession);

                    ControlMessageDTO controlMessageDTO = new ControlMessageDTO();
                    controlMessageDTO.setMessageType(ControlMessageType.CHAT_READY);
                    controlMessageDTO.setTimeProcessed(LocalDateTime.now());
                    controlMessageDTO.setDoctor(consultationSession.getDoctor().getCoreUser().getUserName());
                    controlMessageDTO.setPatient(consultationSession.getConsultation()
                            .getPatient().getCoreUser().getUserName());

                    String controlComunicationTopic = "/topic/consultation/" + consultationSession.getConsultation().getConsultationId() + "" +
                            "/session/" + consultationSession.getId() + "/control";
                    messagingTemplate.convertAndSend(controlComunicationTopic, controlMessageDTO);
                }
            }
        }
    }
}
