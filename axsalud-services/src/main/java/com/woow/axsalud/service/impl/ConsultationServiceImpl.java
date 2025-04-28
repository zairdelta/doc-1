package com.woow.axsalud.service.impl;

import com.woow.axsalud.data.client.AxSaludWooUser;
import com.woow.axsalud.data.consultation.*;
import com.woow.axsalud.data.repository.*;
import com.woow.axsalud.service.api.ConsultationService;
import com.woow.axsalud.service.api.dto.*;
import com.woow.axsalud.service.api.exception.ConsultationServiceException;
import com.woow.core.data.repository.WoowUserRepository;
import com.woow.core.data.user.WoowUser;
import com.woow.core.service.api.exception.WooUserServiceException;
import com.woow.storage.api.StorageService;
import com.woow.storage.api.StorageServiceException;
import com.woow.storage.api.StorageServiceUploadResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class ConsultationServiceImpl implements ConsultationService {

    @Value("${woow.system.user:master@example.com}")
    private String SYSTEM_USER;
    private ConsultationRepository consultationRepository;
    private WoowUserRepository woowUserRepository;
    private AxSaludUserRepository axSaludUserRepository;
    private ConsultationMessageRepository consultationMessageRepository;
    private ModelMapper modelMapper;
    private SimpMessagingTemplate messagingTemplate;
    private StorageService storageService;
    private ConsultationDocumentRepository consultationDocumentRepository;
    private ConsultationSessionRepository consultationSessionRepository;

    public ConsultationServiceImpl(ConsultationRepository consultationRepository,
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
    @Transactional
    public void handledConsultationMessage(final ConsultationMessageDTO consultationMessage) {
        try {
            log.debug("Validating Message: {}", consultationMessage);
            validateOnSend(consultationMessage);
            validate(consultationMessage.getConsultationId(),
                    consultationMessage.getConsultationSessionId(),
                    consultationMessage.getReceiver(), consultationMessage.getSender());
            addMessage(consultationMessage);
        } catch (ConsultationServiceException e) {

            ConsultationMessageDTO errorMessage = new ConsultationMessageDTO();
            errorMessage.setConsultationId(consultationMessage.getConsultationId());
            errorMessage.setSender(SYSTEM_USER);
            errorMessage.setReceiver(consultationMessage.getSender());
            errorMessage.setContent("❌ Failed to send message: " + e.getMessage());
            errorMessage.setMessageType("ERROR");

            log.error("Error validating or adding message for consultationMessage: {}, errorMessage:{}, reporting to system user: {}",
                    consultationMessage, errorMessage, SYSTEM_USER);

            try {
                addMessage(errorMessage);
            } catch (ConsultationServiceException ex) {
                log.error("There was an error processing errorMessage: {}", e.getMessage());
            }

            messagingTemplate.convertAndSendToUser(
                    consultationMessage.getSender(),
                    "/queue/errors",
                    errorMessage
            );
        }
        messagingTemplate.convertAndSendToUser(
                consultationMessage.getReceiver(),
                "/queue/messages",
                consultationMessage
        );
    }

    private void validateOnSend(final ConsultationMessageDTO consultationMessage) throws ConsultationServiceException {
        if(consultationMessage.getReceiver() == null) {
            throw new ConsultationServiceException("Receiver cannot be null while sending a message", 401);
        }
    }

    @Override
    public ConsultationDTO create(SymptomsDTO symptomsDTO, String userName)
            throws WooUserServiceException {
        WoowUser patient = woowUserRepository.findByUserName(userName);
        if(patient == null) {
            log.warn("Consultation could not be created, patient not found: {}",
                    userName);
            throw new WooUserServiceException("User not found", 404);
        }

        log.info("Creating consultation for userName: {}, userId:{}",
                patient.getUserName(), patient.getUserId());

        Optional<AxSaludWooUser> axSaludWooUserOptional =
                axSaludUserRepository.findByCoreUser_UserId(patient.getUserId());

        AxSaludWooUser axSaludPatient =
                axSaludWooUserOptional
                        .orElseThrow(() -> new WooUserServiceException("User not found, principal " +
                "user not configured, " + patient.getUserName(), 404));

        Consultation consultation = new Consultation();
        consultation.setPatient(axSaludPatient);

        consultation.setSymptoms(symptomsDTO.getText());
        consultation.setStatus(ConsultationStatus.WAITING_FOR_DOCTOR);

        ConsultationSession consultationSession = new ConsultationSession();
        consultationSession.setConsultation(consultation);
        consultationSession.setStatus(ConsultationSessionStatus.WAITING_FOR_DOCTOR);
        consultation.getSessions().add(consultationSession);

        consultationRepository.save(consultation);

        ConsultationDTO consultationDTO = modelMapper.map(consultation, ConsultationDTO.class);
        consultationDTO.setPatient(patient.getUserName());
        consultationDTO.setSymptoms(symptomsDTO.getText());
        consultationDTO.setCurrentSessionIdIfExists(consultationSession.getConsultationSessionId().toString());

        messagingTemplate.convertAndSend("/topic/new-patient", consultationDTO);

        return consultationDTO;
    }

    @Override
    public void validate(String consultationId,
                         String consultationSessionId,
                         String receiver, String sender)
            throws ConsultationServiceException {
        log.info("Validating consultationId: {}, receiver: {}, sender: {}", consultationSessionId, receiver, sender);
        ConsultationSession consultationSession =
                consultationSessionRepository.findByConsultationSessionId(UUID.fromString(consultationSessionId));

        if(consultationSession == null) {
            throw new ConsultationServiceException("Invalid Consultation Session, ", 402);
        }

        if(ObjectUtils.isEmpty(receiver)) {
            throw new ConsultationServiceException("Receiver cannot be empty  "
                    + consultationSession.getStatus(), 402);
        }

        if(ObjectUtils.isEmpty(sender)) {
            throw new ConsultationServiceException("Sender cannot be empty  "
                    + consultationSession.getStatus(), 402);
        }

        //TODO Add validation to check consultationSessionId belongs to the same consultation
        Consultation consultation =
                consultationRepository.findByConsultationId(UUID.fromString(consultationId));



        if(!(receiver.equalsIgnoreCase(consultation.getPatient().getCoreUser().getUserName()) ||
                receiver.equalsIgnoreCase(consultationSession.getDoctor().getCoreUser().getUserName()))) {
            throw new ConsultationServiceException("Receiver cannot access consultation:  " + consultation.getConsultationId()
                    + consultationSession.getStatus(), 405);
        }

        if(!(sender.equalsIgnoreCase(consultation.getPatient().getCoreUser().getUserName()) ||
                sender.equalsIgnoreCase(consultationSession.getDoctor().getCoreUser().getUserName()))) {
            throw new ConsultationServiceException("Sender cannot access consultation:  " + consultation.getConsultationId()
                    + consultation.getStatus(), 405);
        }

    }

    @Override
    public ConsultationDTO assign(String doctor, String consultationId, String consultationSessionId) throws ConsultationServiceException {
        Consultation consultation = consultationRepository.findByConsultationId(UUID.fromString(consultationId));

        log.info("Assigning doctor: {} to consultationId: {}", doctor, consultationId);

        if(consultation == null) {
            throw new ConsultationServiceException("invalid consultationId: " + consultationId, 402);
        }

        WoowUser woowUser = woowUserRepository.findByUserName(doctor);

        if(woowUser == null) {
            throw new ConsultationServiceException("User does not exist: " + doctor + ", consultationId: "
                    + consultationId, 402);
        }

        Optional<AxSaludWooUser> axSaludWooUserOptional =
                axSaludUserRepository.findByCoreUser_UserId(woowUser.getUserId());
        if(axSaludWooUserOptional.isEmpty()) {
            throw new ConsultationServiceException("Health User does not exist: " + doctor + ", consultationId: "
                    + consultationId, 402);
        }

        AxSaludWooUser axSaludWooUser = axSaludWooUserOptional.get();
        ConsultationSession consultationSession = consultationSessionRepository
                .findByConsultationSessionId(UUID.fromString(consultationSessionId));

        if(consultationSession == null) {
            throw new ConsultationServiceException("consultationSession: " + consultationSessionId + " does not exist, consultationId: "
                    + consultationId, 402);
        }

        consultationSession.setDoctor(axSaludWooUser);
        consultationSession.setStartAt(LocalDateTime.now());
        consultationSession.setStatus(ConsultationSessionStatus.ON_GOING);

        consultation.setStatus(ConsultationStatus.ON_GOING);

        if(ConsultationStatus.WAITING_FOR_DOCTOR == consultation.getStatus()) {
            consultation.setStartedAt(LocalDateTime.now());
        }

        ConsultationSessionIdDTO consultationSessionIdDTO = new ConsultationSessionIdDTO();
        DoctorViewDTO doctorViewDTO = new DoctorViewDTO();
        modelMapper.map(consultationSession.getDoctor(), doctorViewDTO);
        consultationSessionIdDTO.setDoctorViewDTO(doctorViewDTO);
        consultationSessionIdDTO.setStartAt(LocalDateTime.now());
        consultationSessionIdDTO.setConsultationSessionId(consultationSession
                .getConsultationSessionId().toString());

        ConsultationDTO consultationDTO = new ConsultationDTO();
        consultationDTO.setWelcomeMessage(axSaludWooUser.getDoctorWelcomeMessage());
        consultationDTO.setPatient(consultation.getPatient().getCoreUser().getUserName());
        consultationDTO.getConsultationSessionIdDTOList().add(consultationSessionIdDTO);
        consultationDTO.setCurrentSessionIdIfExists(consultationSessionId);
        consultationDTO.setConsultationId(consultation.getConsultationId().toString());

        ConsultationMessageDTO welcomeMessage = new ConsultationMessageDTO();
        welcomeMessage.setSender(doctor);
        welcomeMessage.setReceiver(consultationDTO.getPatient());
        welcomeMessage.setConsultationId(consultationId);
        welcomeMessage.setConsultationSessionId(consultationSession.getConsultationSessionId().toString());
        welcomeMessage.setContent("👋 " + consultationDTO.getWelcomeMessage());
        welcomeMessage.setMessageType("WELCOME");

        log.info("Sending Welcome message:{} ", welcomeMessage);
        addMessage(welcomeMessage);

        messagingTemplate.convertAndSendToUser(
                consultationDTO.getPatient(),
                "/queue/messages",
                welcomeMessage
        );

        consultationRepository.save(consultation);

        return consultationDTO;

    }

    @Override
    public void addMessage(ConsultationMessageDTO consultationMessage) throws ConsultationServiceException {

        log.info("AddMessage received: {}", consultationMessage);
        ConsultationSession consultationSession =
                consultationSessionRepository
                        .findByConsultationSessionId(
                                UUID.fromString(consultationMessage.getConsultationSessionId()));

        ConsultationMessageEntity consultationMessageEntity = new ConsultationMessageEntity();
        consultationMessageEntity.setConsultationSession(consultationSession);
        consultationMessageEntity.setContent(consultationMessage.getContent());

        log.info("Adding message to DB, sentBy: {}", consultationMessage.getSender());

        WoowUser woowUser = woowUserRepository.findByUserName(consultationMessage.getSender());
        log.debug("sender from DB: {}", woowUser);
        if(woowUser == null) {
            throw new ConsultationServiceException("User not found: " + consultationMessage.getSender(), 404);
        }

        Optional<AxSaludWooUser> axSaludWooUserOptional =
                axSaludUserRepository.findByCoreUser_UserId(woowUser.getUserId());

        axSaludWooUserOptional.ifPresent(consultationMessageEntity::setSentBy);

        consultationMessageRepository.save(consultationMessageEntity);
        consultationSession.getMessages().add(consultationMessageEntity);
        consultationSessionRepository.save(consultationSession);
    }

    @Override
    public long appendDocument(String userName, String consultationSessionId,
                                 MultipartFile file) throws ConsultationServiceException {
        try {
            WoowUser woowUser = woowUserRepository.findByUserName(userName);
            Optional<AxSaludWooUser> axSaludWooUserOptional =
                    axSaludUserRepository.findByCoreUser_UserId(woowUser.getUserId());

            AxSaludWooUser axSaludWooUser = axSaludWooUserOptional.get();

            ConsultationSession consultationSession =
                    consultationSessionRepository.findByConsultationSessionId(UUID.fromString(consultationSessionId));
            StorageServiceUploadResponseDTO storageServiceUploadResponseDTO =
                    storageService.uploadFile(file);

            ConsultationDocument doc = new ConsultationDocument();
            doc.setFileName(storageServiceUploadResponseDTO.getOriginalFilename());
            doc.setFileType(storageServiceUploadResponseDTO.getFileType());
            doc.setElementPublicId(storageServiceUploadResponseDTO.getPublicId());
            doc.setSecureUrl(storageServiceUploadResponseDTO.getSecureUrl());
            doc.setFormat(storageServiceUploadResponseDTO.getFormat());
            doc.setVersion(storageServiceUploadResponseDTO.getVersion());

            if (storageServiceUploadResponseDTO.getCreatedAt() != null) {
                doc.setCreatedAt(LocalDateTime.parse(storageServiceUploadResponseDTO.getCreatedAt(),
                        DateTimeFormatter.ISO_DATE_TIME));
            } else {
                doc.setCreatedAt(LocalDateTime.now());
            }

            doc.setUploadedBy(axSaludWooUser);
            doc.setUploaderRole(axSaludWooUser.getUserType());
            doc.setConsultationSession(consultationSession);

            doc = consultationDocumentRepository.save(doc);

            consultationSession.getDocuments().add(doc);
            consultationSessionRepository.save(consultationSession);

            return doc.getId();
        } catch (StorageServiceException e) {
            throw new ConsultationServiceException(e.getMessage(), 301);
        }
    }

    @Override
    public String downloadDocument(String userName, String consultationSessionId, long fileId) throws ConsultationServiceException {
        ConsultationSession consultation =
                consultationSessionRepository.findByConsultationSessionId(UUID.fromString(consultationSessionId));
        Optional<ConsultationDocument> consultationDocumentOptional =
                consultationDocumentRepository.findById(fileId);
        ConsultationDocument consultationDocument = consultationDocumentOptional.get();
        try {
            return storageService.generateSignedUrl(consultationDocument.getElementPublicId(),
                    consultationDocument.getVersion(),
                    consultationDocument.getFormat(), 95000);
        } catch (StorageServiceException e) {
            throw new ConsultationServiceException(e.getMessage(), 301);
        }
    }

    @Override
    public List<ConsultationDTO> getAllConsultation(String userName,
                                                    int pageNumber,
                                                    int elementsPerPage) {
        //consultationRepository.findAllOrderByStatusDesc();
        return null;
    }
}
