package com.woow.axsalud.service.impl;

import com.woow.axsalud.data.client.AxSaludWooUser;
import com.woow.axsalud.data.consultation.*;
import com.woow.axsalud.data.repository.*;
import com.woow.axsalud.service.api.ConsultationService;
import com.woow.axsalud.service.api.dto.*;
import com.woow.axsalud.service.api.exception.ConsultationServiceException;
import com.woow.axsalud.service.api.messages.ConsultationMessageDTO;
import com.woow.core.data.repository.WoowUserRepository;
import com.woow.core.data.user.WoowUser;
import com.woow.core.service.api.exception.WooUserServiceException;
import com.woow.storage.api.StorageService;
import com.woow.storage.api.StorageServiceException;
import com.woow.storage.api.StorageServiceUploadResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private ComentariosMedicosRepository comentariosMedicosRepository;

    public ConsultationServiceImpl(ConsultationRepository consultationRepository,
                                   WoowUserRepository woowUserRepository,
                                   AxSaludUserRepository axSaludUserRepository,
                                   ModelMapper modelMapper,
                                   SimpMessagingTemplate messagingTemplate,
                                   ConsultationMessageRepository consultationMessageRepository,
                                   final ConsultationDocumentRepository consultationDocumentRepository,
                                   final StorageService storageService,
                                   final ConsultationSessionRepository consultationSessionRepository,
                                   final ComentariosMedicosRepository comentariosMedicosRepository) {
        this.consultationRepository = consultationRepository;
        this.woowUserRepository = woowUserRepository;
        this.axSaludUserRepository = axSaludUserRepository;
        this.modelMapper = modelMapper;
        this.consultationMessageRepository = consultationMessageRepository;
        this.messagingTemplate = messagingTemplate;
        this.consultationDocumentRepository = consultationDocumentRepository;
        this.storageService = storageService;
        this.consultationSessionRepository = consultationSessionRepository;
        this.comentariosMedicosRepository = comentariosMedicosRepository;
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
            errorMessage.setMessageType(ConsultationMessgeTypeEnum.ERROR);

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

        log.info("Creating a new consultation for userName: {}, userId:{}",
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
        consultationSessionRepository.save(consultationSession);

        consultation.getSessions().add(consultationSession);



        consultation = consultationRepository.save(consultation);

        ConsultationDTO consultationDTO = ConsultationDTO.from(consultation);
        consultationDTO.setPatient(patient.getUserName());
        consultationDTO.setSymptoms(symptomsDTO.getText());
        consultationDTO.setCurrentSessionIdIfExists(consultationSession.getConsultationSessionId().toString());
        consultationDTO.setConsultationId(consultation.getConsultationId().toString());
        consultation.setCurrentSessionIdIfExists(consultationSession.getConsultationSessionId().toString());
        consultationRepository.save(consultation);
        log.info("Sending consultationDTO to topic/new-patient: {}", consultationDTO);
        messagingTemplate.convertAndSend("/topic/new-patient", consultationDTO);

        return consultationDTO;
    }

    @Override
    public ConsultationDTO continueWithConsultation(String userName, String consultationId)
            throws WooUserServiceException {

        log.info("continue with consultation: {}, patient: {}", consultationId, userName);

        WoowUser patient = woowUserRepository.findByUserName(userName);
        if(patient == null) {
            log.warn("Consultation could not be created, patient not found: {}",
                    userName);
            throw new WooUserServiceException("User not found", 404);
        }

        log.info("Creating consultation for userName: {}, userId:{}",
                patient.getUserName(), patient.getUserId());

        Consultation consultation = consultationRepository
                .findByConsultationId(UUID.fromString(consultationId));

        ConsultationSession consultationSession = new ConsultationSession();
        consultationSession.setConsultation(consultation);
        consultationSession.setStatus(ConsultationSessionStatus.WAITING_FOR_DOCTOR);

        consultationSession = consultationSessionRepository.save(consultationSession);

        consultation.getSessions().add(consultationSession);
        consultation.setCurrentSessionIdIfExists(consultationSession.getConsultationSessionId().toString());

        consultationRepository.save(consultation);

        ConsultationDTO consultationDTO = modelMapper.map(consultation, ConsultationDTO.class);
        consultationDTO.setCurrentSessionIdIfExists(consultationSession.getConsultationSessionId().toString());

        log.info("Sending consultationDTO to topic/new-patient: {}", consultationDTO);
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

    private void validateConsultationSessionParties(String userName, ConsultationSession consultationSession)
            throws ConsultationServiceException {

        Consultation consultation = consultationSession.getConsultation();
        if(!(userName.equalsIgnoreCase(consultation.getPatient().getCoreUser().getUserName()) ||
                userName.equalsIgnoreCase(consultationSession.getDoctor().getCoreUser().getUserName()))) {
            throw new ConsultationServiceException("Receiver cannot access consultation:  " + consultation.getConsultationId()
                    + consultationSession.getStatus(), 405);
        }
    }

    @Override
    public ConsultationDTO assign(String doctor, String consultationId, String consultationSessionId) throws ConsultationServiceException {
        Consultation consultation =
                consultationRepository.findByConsultationId(UUID.fromString(consultationId));

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
        consultationSessionRepository.save(consultationSession);

        consultation.setStatus(ConsultationStatus.ON_GOING);
        consultation
                .setCurrentSessionIdIfExists(consultationSession.getConsultationSessionId().toString());

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
        welcomeMessage.setMessageType(ConsultationMessgeTypeEnum.WELCOME);

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

        log.info("AddMessage received at service layer: {}", consultationMessage);
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
    public FileResponseDTO appendDocument(String userName, String consultationSessionId,
                                          MultipartFile file) throws ConsultationServiceException {
        try {
            WoowUser woowUser = woowUserRepository.findByUserName(userName);
            Optional<AxSaludWooUser> axSaludWooUserOptional =
                    axSaludUserRepository.findByCoreUser_UserId(woowUser.getUserId());

            AxSaludWooUser axSaludWooUser = axSaludWooUserOptional.get();

            ConsultationSession consultationSession =
                    consultationSessionRepository
                            .findByConsultationSessionId(UUID.fromString(consultationSessionId));

            if(consultationSession == null) {
                throw new ConsultationServiceException("consultationSessionId does not exists: " + consultationSessionId, 402);
            }

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
            FileResponseDTO fileResponseDTO = new FileResponseDTO();
            fileResponseDTO.setId(doc.getId());
            fileResponseDTO.setName(doc.getFileName());
            fileResponseDTO.setUrl(doc.getSecureUrl());
            fileResponseDTO.setConsultationSessionId(consultationSessionId);
            fileResponseDTO.setConsultationId(
                    consultationSession.getConsultation().getConsultationId().toString());
            ConsultationMessageDTO consultationMessageDTO =
                    ConsultationMessageDTO.from(fileResponseDTO, userName);
            addMessage(consultationMessageDTO);
            return fileResponseDTO;
        } catch (StorageServiceException e) {
            throw new ConsultationServiceException(e.getMessage(), 301);
        }
    }

    @Override
    public FileResponseDTO downloadDocument(String userName,
                                            String consultationSessionId, long fileId) throws ConsultationServiceException {

        Optional<ConsultationDocument> consultationDocumentOptional =
                consultationDocumentRepository.findById(fileId);
        ConsultationDocument consultationDocument = consultationDocumentOptional.get();
        //try {
        FileResponseDTO fileResponseDTO = new FileResponseDTO();
        fileResponseDTO.setName(consultationDocument.getFileName());
        fileResponseDTO.setId(fileId);
        fileResponseDTO.setUrl(consultationDocument.getSecureUrl());

        consultationDocument.setLastAccessedAt(LocalDateTime.now());
        consultationDocumentRepository.save(consultationDocument);
        return fileResponseDTO;

            /*
            return  storageService.generateSignedUrl(consultationDocument.getElementPublicId(),
                    consultationDocument.getVersion(),
                    consultationDocument.getFormat(), 95000);

             */
        //  } catch (StorageServiceException e) {
        //     throw new ConsultationServiceException(e.getMessage(), 301);
        //  }

    }

    @Override
    public ConsultationDTO getbyConsultationId(String userName, String consultationId) {

        Consultation consultation =
                consultationRepository.findByConsultationId(UUID.fromString(consultationId));
        return ConsultationDTO.from(consultation);
    }

    @Override
    public List<ConsultationDTO> getConsultationsByStatus(ConsultationStatus status) {
        List<Consultation> consultations =
                consultationRepository.findByStatusOrderByCreatedAtAsc(status);

        return consultations.stream()
                .filter(Objects::nonNull)
                .map(ConsultationDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    public ConsultationMessagesPagingDTO
    getAllMessageByUserNameUsingPaginationPagination(String userName,
                                                     int pageNumber,
                                                     int totalElementsPerPage)
            throws ConsultationServiceException {

        Pageable pageable = PageRequest.of(pageNumber, totalElementsPerPage);
        Page<ConsultationMessageEntity> page = consultationMessageRepository
                .findMessagesByPatientUserNameOrdered(userName, pageable);

        List<ConsultationMessageEntity> messages = page.getContent();
        long totalElements = page.getTotalElements();
        int totalPages = page.getTotalPages();

        ConsultationMessagesPagingDTO consultationMessagesPagingDTO = new ConsultationMessagesPagingDTO();
        consultationMessagesPagingDTO.setMessages(messages.stream()
                .filter(Objects::nonNull)
                .map(ConsultationMessageDTO::from)
                .collect(Collectors.toList()));
        consultationMessagesPagingDTO.setTotalElements(totalElements);
        consultationMessagesPagingDTO.setTotalPages(totalPages);

        return consultationMessagesPagingDTO;
    }

    @Override
    public ConsultationMessagesPagingDTO getAllMessageBySessionIdUsingPaginationPagination(String sessionId,
                                                                                                     int pageNumber,
                                                                                                     int totalElementsPerPage)
            throws ConsultationServiceException {
        Pageable pageable = PageRequest.of(pageNumber, totalElementsPerPage);
        Page<ConsultationMessageEntity> page = consultationMessageRepository
                .findMessagesByConsultationSessionId(UUID.fromString(sessionId), pageable);

        List<ConsultationMessageEntity> messages = page.getContent();
        long totalElements = page.getTotalElements();
        int totalPages = page.getTotalPages();

        ConsultationMessagesPagingDTO consultationMessagesPagingDTO = new ConsultationMessagesPagingDTO();
        consultationMessagesPagingDTO.setMessages(messages.stream()
                .filter(Objects::nonNull)
                .map(ConsultationMessageDTO::from)
                .collect(Collectors.toList()));
        consultationMessagesPagingDTO.setTotalElements(totalElements);
        consultationMessagesPagingDTO.setTotalPages(totalPages);

        return consultationMessagesPagingDTO;
    }

    @Override
    public ConsultationMessagesPagingDTO getAllMessagesGivenConsultationIdAndSessionId(String consultationId,
                                                                                       String consultationSessionId,
                                                                                       int pageNumber, int totalElementsPerPage)
            throws ConsultationServiceException {

        if(ObjectUtils.isEmpty(consultationSessionId)) {
            throw new ConsultationServiceException("consultationSessionId cannot be empty", 402);
        }

        Pageable pageable = PageRequest.of(pageNumber, totalElementsPerPage);
        Page<ConsultationMessageEntity> page = consultationMessageRepository
                .findMessagesByConsultationSessionId(UUID.fromString(consultationSessionId), pageable);

        List<ConsultationMessageEntity> messages = page.getContent();
        long totalElements = page.getTotalElements();
        int totalPages = page.getTotalPages();

        ConsultationMessagesPagingDTO consultationMessagesPagingDTO = new ConsultationMessagesPagingDTO();
        consultationMessagesPagingDTO.setMessages(messages.stream()
                .filter(Objects::nonNull)
                .map(ConsultationMessageDTO::from)
                .collect(Collectors.toList()));
        consultationMessagesPagingDTO.setTotalElements(totalElements);
        consultationMessagesPagingDTO.setTotalPages(totalPages);

        return consultationMessagesPagingDTO;
    }

    @Override
    public ConsultationSession getConsultationSession(String consultationSessionId)
            throws ConsultationServiceException {
        if(ObjectUtils.isEmpty(consultationSessionId)) {
            throw new ConsultationServiceException("invalid consultationSessionId: " +
                    consultationSessionId, 402);
        }
        ConsultationSession consultationSession =
                consultationSessionRepository.findByConsultationSessionId(UUID.fromString(consultationSessionId));

        return consultationSession;
    }

    @Override
    public void closeSession(String consultationId, String consultationSessionId, String sender)
            throws ConsultationServiceException {

        ConsultationSession consultationSession =
                consultationSessionRepository
                        .findByConsultationSessionId(UUID.fromString(consultationSessionId));

        if(!ConsultationSessionStatus.FINISHED
                .getStatus().equalsIgnoreCase(consultationSession.getStatus().toString())) {
            consultationSession.setStatus(ConsultationSessionStatus.FINISHED);
            consultationSession.setFinishedAt(LocalDateTime.now());
            consultationSession.getConsultation().setStatus(ConsultationStatus.FINISHED);
            consultationSession.getConsultation().setFinishedAt(LocalDateTime.now());
            consultationRepository.save(consultationSession.getConsultation());
        }

        consultationSession.getConsultation().setCurrentSessionIdIfExists(null);
        consultationSession.getConsultation().setStatus(ConsultationStatus.FINISHED);
        consultationRepository.save(consultationSession.getConsultation());
        consultationSession.getClosedBy().add(sender);
        consultationSessionRepository.save(consultationSession);

        String patientUserName = consultationSession
                .getConsultation()
                .getPatient().getCoreUser().getUserName();
        String doctorUserName = consultationSession.getDoctor().getCoreUser().getUserName();

        String receiver = doctorUserName.equalsIgnoreCase(sender) ?
                patientUserName : doctorUserName;

        ConsultationMessageDTO endSessionMessageDTO = new ConsultationMessageDTO();
        endSessionMessageDTO.setSender(patientUserName);
        endSessionMessageDTO.setReceiver(receiver);
        endSessionMessageDTO.setConsultationId(consultationId);
        endSessionMessageDTO.setConsultationSessionId(consultationSession.getConsultationSessionId().toString());
        endSessionMessageDTO.setContent(" ");
        endSessionMessageDTO.setMessageType(ConsultationMessgeTypeEnum.SESSION_END);
        addMessage(endSessionMessageDTO);

        messagingTemplate.convertAndSendToUser(
                receiver,
                "/queue/messages",
                endSessionMessageDTO
        );

    }

    @Override
    public ConsultationSessionViewDTO getConsultationSession(String userName, String consultationSessionId)
            throws ConsultationServiceException {

        log.info("getting consultation Session details for: {}, sessionID: {}", userName,
                consultationSessionId);
        if(ObjectUtils.isEmpty(consultationSessionId) ||
                ObjectUtils.isEmpty(userName)) {
            throw new ConsultationServiceException("userName and consultationSessionId are mandatory", 401);
        }

        ConsultationSession consultationSession = consultationSessionRepository
                .findByConsultationSessionId(UUID.fromString(consultationSessionId));
        validateConsultationSessionParties(userName, consultationSession);
        ConsultationSessionViewDTO consultationSessionViewDTO = new ConsultationSessionViewDTO();
        consultationSessionViewDTO.setConsultationId(consultationSession.getConsultation().getConsultationId().toString());
        consultationSessionViewDTO.setConsultationSessionId(consultationSessionId);
        consultationSessionViewDTO.setStartAt(consultationSession.getStartAt());
        consultationSessionViewDTO.setFinishedAt(consultationSession.getFinishedAt());

        Consultation consultation = consultationSession.getConsultation();

        WoowUser patientWoowUser = consultation.getPatient().getCoreUser();
        WoowUser doctorWoowUser = consultationSession.getDoctor().getCoreUser();
        DoctorViewDTO doctorViewDTO = new DoctorViewDTO();
        doctorViewDTO.setName(doctorWoowUser.getName());
        doctorViewDTO.setLastName(doctorWoowUser.getLastName());

        PatientViewDTO patientViewDTO = new PatientViewDTO();
        patientViewDTO.setName(patientWoowUser.getName());
        patientViewDTO.setLastName(patientWoowUser.getLastName());
        consultationSessionViewDTO.setDoctorViewDTO(doctorViewDTO);
        consultationSessionViewDTO.setPatientViewDTO(patientViewDTO);
        consultationSessionViewDTO.setDoctorPrescriptions(consultationSession.getDoctorPrescriptions());
        consultationSessionViewDTO.setLaboratoryPrescriptions(consultationSession.getLaboratoryPrescriptions());
        return consultationSessionViewDTO;
    }

    @Override
    public void addDoctorPrescriptions(String userName, String consultationId,
                                       String consultationSessionId,
                                       List<DoctorPrescriptionDTO> doctorPrescriptionDTOS) throws ConsultationServiceException {
        ConsultationSession consultationSession =
                consultationSessionRepository.findByConsultationSessionId(UUID.fromString(consultationSessionId));
        validateConsultationSessionParties(userName, consultationSession);

        if(!CollectionUtils.isEmpty(doctorPrescriptionDTOS)) {
            Set<DoctorPrescription> doctorPrescriptionSet =
                    doctorPrescriptionDTOS.stream()
                            .map(doctorPrescription -> {
                                DoctorPrescription doctorPrescription1 = new DoctorPrescription();

                                modelMapper.map(doctorPrescription, doctorPrescription1);
                                doctorPrescription1.setConsultationSession(consultationSession);

                                if(!ObjectUtils.isEmpty(doctorPrescription.getComentariosMedicos())) {
                                    ComentariosMedicos comentariosMedicos = new ComentariosMedicos();
                                    comentariosMedicos.setObservacionesMedicas(doctorPrescription.getComentariosMedicos());
                                    comentariosMedicos
                                            .setAxSaludWooUser(consultationSession.getConsultation().getPatient());
                                    comentariosMedicos.setConsultationSession(consultationSession);
                                    comentariosMedicos = comentariosMedicosRepository.save(comentariosMedicos);
                                    consultationSession.setComentariosMedicos(comentariosMedicos);
                                    consultationSessionRepository.save(consultationSession);

                                }

                                return doctorPrescription1;
                            })
                            .collect(Collectors.toSet());
            consultationSession.getDoctorPrescriptions().addAll(doctorPrescriptionSet);
        }
    }

    @Override
    public void addLaboratoryPrescriptions(String userName, String consultationId,
                                           String consultationSessionId,
                                           List<LaboratoryPrescriptionDTO> laboratoryPrescriptions)
            throws ConsultationServiceException {
        ConsultationSession consultationSession =
                consultationSessionRepository.findByConsultationSessionId(UUID.fromString(consultationSessionId));
        validateConsultationSessionParties(userName, consultationSession);

        if(!CollectionUtils.isEmpty(laboratoryPrescriptions)) {
            Set<LaboratoryPrescription> laboratoryPrescriptionsSet =
                    laboratoryPrescriptions.stream()
                            .map(laboratoryPrescription -> {
                                LaboratoryPrescription laboratoryPrescription1 = new LaboratoryPrescription();
                                laboratoryPrescription.setId(0);
                                modelMapper.map(laboratoryPrescription, laboratoryPrescription1);
                                return laboratoryPrescription1;
                            })
                            .collect(Collectors.toSet());
            consultationSession.getLaboratoryPrescriptions().addAll(laboratoryPrescriptionsSet);
        }
    }

}
