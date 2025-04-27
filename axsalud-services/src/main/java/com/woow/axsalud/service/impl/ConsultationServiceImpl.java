package com.woow.axsalud.service.impl;

import com.woow.axsalud.data.client.AxSaludWooUser;
import com.woow.axsalud.data.consultation.Consultation;
import com.woow.axsalud.data.consultation.ConsultationMessageEntity;
import com.woow.axsalud.data.consultation.ConsultationStatus;
import com.woow.axsalud.data.consultation.ConsultationDocument;
import com.woow.axsalud.data.repository.AxSaludUserRepository;
import com.woow.axsalud.data.repository.ConsultationDocumentRepository;
import com.woow.axsalud.data.repository.ConsultationMessageRepository;
import com.woow.axsalud.data.repository.ConsultationRepository;
import com.woow.axsalud.service.api.ConsultationService;
import com.woow.axsalud.service.api.dto.ConsultationDTO;
import com.woow.axsalud.service.api.dto.ConsultationMessage;
import com.woow.axsalud.service.api.dto.SymptomsDTO;
import com.woow.axsalud.service.api.exception.ConsultationServiceException;
import com.woow.core.data.repository.WoowUserRepository;
import com.woow.core.data.user.WoowUser;
import com.woow.core.service.api.exception.WooUserServiceException;
import com.woow.storage.api.StorageService;
import com.woow.storage.api.StorageServiceException;
import com.woow.storage.api.StorageServiceUploadResponseDTO;
import io.jsonwebtoken.lang.Strings;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class ConsultationServiceImpl implements ConsultationService {

    private ConsultationRepository consultationRepository;
    private WoowUserRepository woowUserRepository;
    private AxSaludUserRepository axSaludUserRepository;
    private ConsultationMessageRepository consultationMessageRepository;
    private ModelMapper modelMapper;
    private SimpMessagingTemplate messagingTemplate;
    private StorageService storageService;
    private ConsultationDocumentRepository consultationDocumentRepository;

    public ConsultationServiceImpl(ConsultationRepository consultationRepository,
                                   WoowUserRepository woowUserRepository,
                                   AxSaludUserRepository axSaludUserRepository,
                                   ModelMapper modelMapper,
                                   SimpMessagingTemplate messagingTemplate,
                                   ConsultationMessageRepository consultationMessageRepository,
                                   final ConsultationDocumentRepository consultationDocumentRepository,
                                   final StorageService storageService) {
        this.consultationRepository = consultationRepository;
        this.woowUserRepository = woowUserRepository;
        this.axSaludUserRepository = axSaludUserRepository;
        this.modelMapper = modelMapper;
        this.consultationMessageRepository = consultationMessageRepository;
        this.messagingTemplate = messagingTemplate;
        this.consultationDocumentRepository = consultationDocumentRepository;
        this.storageService = storageService;
    }

    @Override
    @Transactional
    public void handledConsultationMessage(ConsultationMessage consultationMessage) {
        try {
            validate(consultationMessage.getConsultationId(),
                    consultationMessage.getReceiver(), consultationMessage.getSender());
            addMessage(consultationMessage);
        } catch (ConsultationServiceException e) {

            ConsultationMessage errorMessage = new ConsultationMessage();
            errorMessage.setConsultationId(consultationMessage.getConsultationId());
            errorMessage.setSender("system");
            errorMessage.setReceiver(consultationMessage.getSender());
            errorMessage.setContent("❌ Failed to send message: " + e.getMessage());
            errorMessage.setMessageType("ERROR");
            addMessage(errorMessage);
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


        consultationRepository.save(consultation);

        ConsultationDTO consultationDTO = modelMapper.map(consultation, ConsultationDTO.class);
        consultationDTO.setDoctor("");
        consultationDTO.setPatient(patient.getUserName());
        consultationDTO.setSymptoms(symptomsDTO.getText());

        messagingTemplate.convertAndSend("/topic/new-patient", consultationDTO);

        return consultationDTO;
    }

    @Override
    public void validate(String consultationId, String receiver, String sender)
            throws ConsultationServiceException {
        Consultation consultation =
                consultationRepository.findByConsultationId(UUID.fromString(consultationId));

        if(consultation == null) {
            throw new ConsultationServiceException("Invalid Consultation, ", 402);
        }

        if(ObjectUtils.isEmpty(receiver)) {
            throw new ConsultationServiceException("Receiver cannot be empty  "
                    + consultation.getStatus(), 402);
        }

        if(ObjectUtils.isEmpty(sender)) {
            throw new ConsultationServiceException("Sender cannot be empty  "
                    + consultation.getStatus(), 402);
        }

        if(!(receiver.equalsIgnoreCase(consultation.getPatient().getCoreUser().getUserName()) ||
                receiver.equalsIgnoreCase(consultation.getDoctor().getCoreUser().getUserName()))) {
            throw new ConsultationServiceException("Receiver cannot access consultation:  " + consultation.getConsultationId()
                    + consultation.getStatus(), 405);
        }

        if(!(sender.equalsIgnoreCase(consultation.getPatient().getCoreUser().getUserName()) ||
                receiver.equalsIgnoreCase(consultation.getDoctor().getCoreUser().getUserName()))) {
            throw new ConsultationServiceException("Sender cannot access consultation:  " + consultation.getConsultationId()
                    + consultation.getStatus(), 405);
        }


    }

    @Override
    public ConsultationDTO assign(String doctor, String consultationId) throws ConsultationServiceException {
        Consultation consultation = consultationRepository.findByConsultationId(UUID.fromString(consultationId));

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
        consultation.setDoctor(axSaludWooUser);
        consultation.setStatus(ConsultationStatus.ON_GOING);

        ConsultationDTO consultationDTO = new ConsultationDTO();
        consultationDTO.setWelcomeMessage(axSaludWooUser.getDoctorWelcomeMessage());
        consultationDTO.setPatient(consultation.getPatient().getCoreUser().getUserName());
        consultationDTO.setDoctor(doctor);
        consultationDTO.setConsultationId(consultation.getConsultationId().toString());

        ConsultationMessage welcomeMessage = new ConsultationMessage();
        welcomeMessage.setSender(doctor);
        welcomeMessage.setReceiver(consultationDTO.getPatient());
        welcomeMessage.setConsultationId(consultationId);
        welcomeMessage.setContent("👋 " + consultationDTO.getWelcomeMessage());
        welcomeMessage.setMessageType("WELCOME");

        addMessage(welcomeMessage);

        messagingTemplate.convertAndSendToUser(
                consultationDTO.getPatient(),
                "/queue/messages",
                welcomeMessage
        );

        return consultationDTO;

    }

    @Override
    public void addMessage(ConsultationMessage consultationMessage) {
        Consultation consultation =
                consultationRepository.findByConsultationId(UUID.fromString(consultationMessage.getConsultationId()));

        ConsultationMessageEntity consultationMessageEntity = new ConsultationMessageEntity();
        consultationMessageEntity.setConsultation(consultation);
        consultationMessageEntity.setContent(consultationMessage.getContent());

        WoowUser woowUser = woowUserRepository.findByUserName(consultationMessage.getSender());

        Optional<AxSaludWooUser> axSaludWooUserOptional =
                axSaludUserRepository.findByCoreUser_UserId(woowUser.getUserId());

        consultationMessageEntity.setSentBy(axSaludWooUserOptional.get());
        consultationMessageRepository.save(consultationMessageEntity);
        consultation.getMessages().add(consultationMessageEntity);
        consultationRepository.save(consultation);
    }

    @Override
    public long appendDocument(String userName, String consultationId,
                                 MultipartFile file) throws ConsultationServiceException {
        try {
            WoowUser woowUser = woowUserRepository.findByUserName(userName);
            Optional<AxSaludWooUser> axSaludWooUserOptional =
                    axSaludUserRepository.findByCoreUser_UserId(woowUser.getUserId());

            AxSaludWooUser axSaludWooUser = axSaludWooUserOptional.get();

            Consultation consultation =
                    consultationRepository.findByConsultationId(UUID.fromString(consultationId));
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
            doc.setConsultation(consultation);

            doc = consultationDocumentRepository.save(doc);

            consultation.getDocuments().add(doc);
            consultationRepository.save(consultation);

            return doc.getId();
        } catch (StorageServiceException e) {
            throw new ConsultationServiceException(e.getMessage(), 301);
        }
    }

    @Override
    public String downloadDocument(String userName, String consultationId, long fileId) throws ConsultationServiceException {
        Consultation consultation =
                consultationRepository.findByConsultationId(UUID.fromString(consultationId));
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
}
