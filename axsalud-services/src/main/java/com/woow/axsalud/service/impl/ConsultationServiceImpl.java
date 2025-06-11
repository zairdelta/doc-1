package com.woow.axsalud.service.impl;

import com.woow.axsalud.common.AXSaludUserRoles;
import com.woow.axsalud.common.UserStatesEnum;
import com.woow.axsalud.data.client.AxSaludWooUser;
import com.woow.axsalud.data.consultation.*;
import com.woow.axsalud.data.repository.*;
import com.woow.axsalud.service.api.ConsultationService;
import com.woow.axsalud.service.api.dto.*;
import com.woow.axsalud.service.api.exception.ConsultationServiceException;
import com.woow.axsalud.service.api.messages.ConsultationEventDTO;
import com.woow.axsalud.service.api.messages.ConsultationMessageDTO;
import com.woow.axsalud.service.api.messages.control.ControlMessageDTO;
import com.woow.axsalud.service.api.messages.control.ControlMessageType;
import com.woow.axsalud.service.api.messages.control.SessionAbandonedDTO;
import com.woow.axsalud.service.impl.websocket.AppOutboundService;
import com.woow.core.data.repository.WoowUserRepository;
import com.woow.core.data.user.WoowUser;
import com.woow.core.service.api.exception.WooUserServiceException;
import com.woow.security.api.ws.PlatformService;
import com.woow.storage.api.StorageService;
import com.woow.storage.api.StorageServiceException;
import com.woow.storage.api.StorageServiceUploadResponseDTO;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PessimisticLockException;
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

import static com.woow.axsalud.common.WoowConstants.NO_TRANSPORT_SESSION;

@Service
@Slf4j
@Transactional
public class ConsultationServiceImpl implements ConsultationService {

    @Value("${woow.system.user:master@example.com}")
    private String SYSTEM_USER;

    private static final String CHAT_PING_MESSAGE = "<messageType>ping</messageType>";
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
    private PlatformService platformService;
    private AppOutboundService appOutboundService;

    public ConsultationServiceImpl(ConsultationRepository consultationRepository,
                                   WoowUserRepository woowUserRepository,
                                   AxSaludUserRepository axSaludUserRepository,
                                   ModelMapper modelMapper,
                                   SimpMessagingTemplate messagingTemplate,
                                   ConsultationMessageRepository consultationMessageRepository,
                                   final PlatformService platformService,
                                   final ConsultationDocumentRepository consultationDocumentRepository,
                                   final StorageService storageService,
                                   final ConsultationSessionRepository consultationSessionRepository,
                                   final ComentariosMedicosRepository comentariosMedicosRepository,
                                   final AppOutboundService appOutboundService) {
        this.consultationRepository = consultationRepository;
        this.woowUserRepository = woowUserRepository;
        this.axSaludUserRepository = axSaludUserRepository;
        this.platformService = platformService;
        this.modelMapper = modelMapper;
        this.consultationMessageRepository = consultationMessageRepository;
        this.messagingTemplate = messagingTemplate;
        this.consultationDocumentRepository = consultationDocumentRepository;
        this.storageService = storageService;
        this.consultationSessionRepository = consultationSessionRepository;
        this.comentariosMedicosRepository = comentariosMedicosRepository;
        this.appOutboundService = appOutboundService;
    }


    @Override
    @Transactional
    public void handledConsultationMessage(final String sessionId, final ConsultationMessageDTO consultationMessage) {

        long eventId = 0;
        try {
            log.debug("{}_ Validating Message: {}", sessionId, consultationMessage);

            if(consultationMessage.getContent() != null &
                    !consultationMessage.getContent().equalsIgnoreCase(CHAT_PING_MESSAGE)) {
                validateOnSend(consultationMessage);
                validate(consultationMessage.getConsultationId(),
                        consultationMessage.getConsultationSessionId(),
                        consultationMessage.getReceiver(), consultationMessage.getSender());
                eventId = addMessage(consultationMessage, ConsultationMessgeTypeEnum.TEXT_MESSAGE);
            } else {
                log.debug("{}_ Receiving messagetype ping, keep alive send to other party in chat topic", sessionId);
            }

            ConsultationEventDTO<ConsultationMessageDTO> consultationEventDTO = new ConsultationEventDTO<>();
            consultationEventDTO.setTransportSessionId(sessionId);
            consultationEventDTO.setMessageType(ConsultationMessgeTypeEnum.TEXT_MESSAGE);
            consultationEventDTO.setTimeProcessed(LocalDateTime.now());
            consultationEventDTO.setPayload(consultationMessage);
            consultationEventDTO.setId(eventId);

            appOutboundService.sendQueueMessage(consultationMessage.getReceiver(), consultationEventDTO);

           /* messagingTemplate.convertAndSendToUser(
                    consultationMessage.getReceiver(),
                    "/queue/messages",
                    consultationEventDTO
            );*/

        } catch (ConsultationServiceException e) {

            log.error("{}_ Error Sending consultationMessage DTO: {}", sessionId, e.getMessage());
            ConsultationMessageDTO errorMessage = new ConsultationMessageDTO();
            errorMessage.setConsultationId(consultationMessage.getConsultationId());
            errorMessage.setSender(SYSTEM_USER);
            errorMessage.setReceiver(consultationMessage.getSender());
            errorMessage.setContent("❌ Failed to send message: " + e.getMessage());

            ConsultationEventDTO<ConsultationMessageDTO> consultationEventDTO = new ConsultationEventDTO<>();
            consultationEventDTO.setMessageType(ConsultationMessgeTypeEnum.ERROR);
            consultationEventDTO.setTimeProcessed(LocalDateTime.now());
            consultationEventDTO.setPayload(errorMessage);

            log.error("{}_ Error validating or adding message for consultationMessage: {}, " +
                            "errorMessage:{}, reporting to system user: {}", sessionId,
                    consultationMessage, errorMessage, SYSTEM_USER);

            try {
                eventId = addMessage(errorMessage, ConsultationMessgeTypeEnum.ERROR);
                consultationEventDTO.setId(eventId);
            } catch (ConsultationServiceException ex) {
                log.error("{}}_ There was an error processing errorMessage: {}", sessionId,
                        e.getMessage());
            }
            appOutboundService.sendErrorQueueMessage(consultationMessage.getSender(), consultationEventDTO);
           /* messagingTemplate.convertAndSendToUser(
                    consultationMessage.getSender(),
                    "/queue/errors",
                    consultationEventDTO
            ); */
        }

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

        ConsultationDTO consultationDTO =
                createConsultationDTO(consultation, consultationSession, symptomsDTO.getText());

        consultation.setCurrentSessionIdIfExists(consultationSession.getConsultationSessionId().toString());
        consultationRepository.save(consultation);

        sendConsultationDTOToDoctorEvents(NO_TRANSPORT_SESSION, consultationDTO);

        return consultationDTO;
    }

    private void sendConsultationDTOToDoctorEvents(String transportSessionId, ConsultationDTO consultationDTO) {
        log.debug("{}_ Sending consultationDTO to doctor events, consultationDTO: {}", transportSessionId, consultationDTO);
        ConsultationEventDTO<ConsultationDTO> consultationEventDTO = new ConsultationEventDTO<>();
        consultationEventDTO.setMessageType(ConsultationMessgeTypeEnum.NEW_CONSULTATION_CREATED);
        consultationEventDTO.setTimeProcessed(LocalDateTime.now());
        consultationEventDTO.setPayload(consultationDTO);
        consultationEventDTO.setId(0);

        /*log.info("Sending consultationDTO to topic/doctor-events: {}", consultationEventDTO);
        messagingTemplate.convertAndSend("/topic/doctor-events", consultationEventDTO);*/
        appOutboundService.sendDoctorEventMessage(consultationEventDTO);
    }

    private ConsultationDTO createConsultationDTO(Consultation consultation, ConsultationSession consultationSession,
                                                  String symptoms) {
        ConsultationDTO consultationDTO = ConsultationDTO.from(consultation);
        consultationDTO.setPatient(consultation.getPatient().getCoreUser().getUserName());
        consultationDTO.setSymptoms(symptoms);
        consultationDTO.setCurrentSessionIdIfExists(consultationSession.getConsultationSessionId().toString());
        consultationDTO.setConsultationId(consultation.getConsultationId().toString());
        return consultationDTO;
    }

    @Override
    public void addComentariosMedicos(DoctorCommentsDTO doctorCommentsDTO,
                                      String consultationSessionId) {

        ConsultationSession consultationSession = consultationSessionRepository
                .findByConsultationSessionId(UUID.fromString(consultationSessionId));

        if(consultationSession != null) {

            ComentariosMedicos comentariosMedicos = consultationSession.getComentariosMedicos() ;

            if(comentariosMedicos == null) {
                comentariosMedicos = new ComentariosMedicos();
                comentariosMedicos
                        .setAxSaludWooUser(consultationSession.getConsultation().getPatient());
                comentariosMedicos.setConsultationSession(consultationSession);
            }

            comentariosMedicos.setObservacionesMedicas(doctorCommentsDTO.getComment());
            comentariosMedicos = comentariosMedicosRepository.save(comentariosMedicos);
            consultationSession.setComentariosMedicos(comentariosMedicos);
            consultationSessionRepository.save(consultationSession);
        }
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

        log.info("Sending consultationDTO to topic/doctor-events: {}", consultationDTO);
       /* messagingTemplate.convertAndSend("/topic/doctor-events", consultationDTO);*/
        appOutboundService.sendDoctorEventMessage(consultationDTO);

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
    public ConsultationDTO assign(String doctor, String consultationId, String consultationSessionId)
            throws ConsultationServiceException {

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

        ConsultationSession consultationSession = null;

        try {
            consultationSession = consultationSessionRepository
                    .findWithLock(UUID.fromString(consultationSessionId));
        } catch (PessimisticLockException e) {
            log.warn("Could not acquire lock for consultationSessionId: {}", consultationSessionId, e);
            throw new ConsultationServiceException(
                    "Consultation is being assigned to another doctor.", 409
            );
        } catch (NoResultException e) {
            throw new ConsultationServiceException("consultationSession: " + consultationSessionId + " does not exist, consultationId: "
                    + consultationId, 402);
        }

        if(consultationSession.getStatus() != ConsultationSessionStatus.WAITING_FOR_DOCTOR) {
            throw new ConsultationServiceException("Consultation was assigned already to doctor: " +
                    consultationSession.getDoctor().getCoreUser().getName() + ", consultationId: "
                    + consultationId, 402);
        }

       if(consultation.getPatient().getUserAvailability() == UserStatesEnum.OFFLINE) {
           consultationRepository.updateStatus(UUID.fromString(consultationId), ConsultationStatus.ABANDONED);
           consultationSessionRepository.updateStatus(UUID.fromString(consultationSessionId),
                   ConsultationSessionStatus.ABANDONED);
           throw new ConsultationServiceException("Patient is now offline," +
                    " consultation cannot be assigned to: " +
                    doctor + ", consultationId: "
                    + consultationId, 402);
        }


        consultationSession.setDoctor(axSaludWooUser);
        consultationSession.setStartAt(LocalDateTime.now());
        consultationSession.setStatus(ConsultationSessionStatus.CONFIRMING_PARTIES);
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
        consultationDTO.setDoctor(consultationSession.getDoctor().getCoreUser().getUserName());

        ConsultationEventDTO<ConsultationMessageDTO> consultationEventDTO = new ConsultationEventDTO<>();
        consultationEventDTO.setMessageType(ConsultationMessgeTypeEnum.WELCOME);
        consultationEventDTO.setTimeProcessed(LocalDateTime.now());


        ConsultationMessageDTO welcomeMessage = new ConsultationMessageDTO();
        welcomeMessage.setSender(doctor);
        welcomeMessage.setReceiver(consultationDTO.getPatient());
        welcomeMessage.setConsultationId(consultationId);
        welcomeMessage.setConsultationSessionId(consultationSession.getConsultationSessionId().toString());
        welcomeMessage.setContent("👋 " + consultationDTO.getWelcomeMessage());

       // log.info("Sending Welcome message:{} ", welcomeMessage);
        // no need to store welcome message
        //long eventId = addMessage(welcomeMessage, ConsultationMessgeTypeEnum.WELCOME);
        //consultationEventDTO.setId(eventId);
        consultationEventDTO.setPayload(welcomeMessage);

      /*  messagingTemplate.convertAndSendToUser(
                consultationDTO.getPatient(),
                "/queue/messages",
                consultationEventDTO
        );
*/
        consultationRepository.save(consultation);

        consultationEventDTO.setMessageType(ConsultationMessgeTypeEnum.CONSULTATION_ASSIGNED);
        log.info("new consultationDTO assigned to topic/doctor-events: {}", consultationEventDTO);
        appOutboundService.sendDoctorEventMessage(consultationEventDTO);
      //  messagingTemplate.convertAndSend("/topic/doctor-events", consultationEventDTO);

        ControlMessageDTO controlMessageDTO = new ControlMessageDTO();
        controlMessageDTO.setMessageType(ControlMessageType.DOCTOR_ASSIGNED);
        controlMessageDTO.setTimeProcessed(LocalDateTime.now());
        controlMessageDTO.setDoctor(consultationSession.getDoctor().getCoreUser().getUserName());
        controlMessageDTO.setPatient(consultationSession.getConsultation()
                .getPatient().getCoreUser().getUserName());

        /*
        String controlComunicationTopic = "/topic/consultation." + consultationSession.getConsultation().getConsultationId() +
                ".session." + consultationSession.getConsultationSessionId() + ".control";
        log.debug("Sending controleMessage to topic: {} ", controlComunicationTopic);
        messagingTemplate.convertAndSend(controlComunicationTopic, controlMessageDTO);
        */

        appOutboundService.sendConsultationControlEvent(consultationId, consultationSessionId, controlMessageDTO);
        //log.debug("ControlMessage sent to topic: {}, message: {} ", controlComunicationTopic, controlMessageDTO);

        return consultationDTO;
    }

    @Override
    public long addMessage(ConsultationMessageDTO consultationMessage,
                           ConsultationMessgeTypeEnum consultationMessageType)
            throws ConsultationServiceException {

        log.info("AddMessage received at service layer: {}", consultationMessage);
        ConsultationSession consultationSession =
                consultationSessionRepository
                        .findByConsultationSessionId(
                                UUID.fromString(consultationMessage.getConsultationSessionId()));

        ConsultationMessageEntity consultationMessageEntity = new ConsultationMessageEntity();
        consultationMessageEntity.setConsultationSession(consultationSession);
        consultationMessageEntity.setContent(consultationMessage.getContent());
        consultationMessageEntity.setMessageType(consultationMessageType.getType().toString());

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
        return consultationSessionRepository.save(consultationSession).getId();
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
            ConsultationEventDTO<ConsultationMessageDTO> consultationMessageDTO =
                    ConsultationMessageDTO.from(fileResponseDTO, userName);
            addMessage(consultationMessageDTO.getPayload(), ConsultationMessgeTypeEnum.FILE_UPLOADED);
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
        log.info("getting messages by userName: {}", userName);
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
        log.info("getting messages for sessionId: {}", sessionId);
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

        consultationMessagesPagingDTO.getMessages()
                .forEach(System.out::println);

        return consultationMessagesPagingDTO;
    }

    @Override
    public ConsultationMessagesPagingDTO getAllMessagesGivenConsultationIdAndSessionId(String consultationId,
                                                                                       String consultationSessionId,
                                                                                       int pageNumber, int totalElementsPerPage)
            throws ConsultationServiceException {

        log.info("getting messages for consultationID: {}, sessionId: {}", consultationId, consultationSessionId);
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
                .filter(consultationMessageEntity ->
                        ConsultationMessgeTypeEnum.TEXT_MESSAGE.getType().equalsIgnoreCase(consultationMessageEntity.getMessageType()) ||
                                ConsultationMessgeTypeEnum.WELCOME.getType().equalsIgnoreCase(consultationMessageEntity.getMessageType()) ||
                                ConsultationMessgeTypeEnum.FILE_UPLOADED.getType().equalsIgnoreCase(consultationMessageEntity.getMessageType()))
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
    public void closeSession(String sessionId,
                             String consultationId, String consultationSessionId, String sender)
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


        long eventId = addMessage(endSessionMessageDTO, ConsultationMessgeTypeEnum.SESSION_END);

        ConsultationEventDTO<ConsultationMessageDTO> consultationEventDTO = new ConsultationEventDTO<>();
        consultationEventDTO.setMessageType(ConsultationMessgeTypeEnum.SESSION_END);
        consultationEventDTO.setTimeProcessed(LocalDateTime.now());
        consultationEventDTO.setPayload(endSessionMessageDTO);
        consultationEventDTO.setId(eventId);

        appOutboundService.sendQueueMessage(receiver, consultationEventDTO);

        /*messagingTemplate.convertAndSendToUser(
                receiver,
                "/queue/messages",
                consultationEventDTO
        );*/

        //messagingTemplate.convertAndSend("/topic/doctor-events", consultationEventDTO);
        appOutboundService.sendDoctorEventMessage(consultationEventDTO);
        String controlComunicationTopic = "/topic/consultation." + consultationSession.getConsultation().getConsultationId() +
                ".session." + consultationSession.getConsultationSessionId() + ".control";
        log.info("{}_ ending application session id: {} ", sessionId, controlComunicationTopic);
        platformService.appSessionTerminated(controlComunicationTopic);

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
                                doctorPrescription1.setComentariosMedicos(doctorPrescription.getComentariosMedicos());
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
                                laboratoryPrescription1.setConsultationSession(consultationSession);
                                return laboratoryPrescription1;
                            })
                            .collect(Collectors.toSet());
            consultationSession.getLaboratoryPrescriptions().addAll(laboratoryPrescriptionsSet);
        }
        consultationSessionRepository.save(consultationSession);
    }

    @Override
    public void consultationDisconnect(String sessionId, String consultationId,
                                       String consultationSessionId, String userName, String role) {
        if(ObjectUtils.isEmpty(consultationSessionId)) {
            log.warn("{}_ consultationSessionId is empty, cannot determinate the consultation, userName:{}," +
                    " consultationId: {}, consultationSessionId: {} ", sessionId, userName,
                    consultationId, consultationSessionId);
        } else {

            UUID consultationSessionUUID = UUID.fromString(consultationSessionId);

            ConsultationSession consultationSession =
                    consultationSessionRepository.findByConsultationSessionId(consultationSessionUUID);
            Consultation consultation = consultationRepository.findByConsultationId(UUID.fromString(consultationId));

            ConsultationSessionStatus status = consultationSession.getStatus();

            if(ConsultationSessionStatus.FINISHED.equals(status)) {
                log.info("{}_ consultationSessionID: {} for user:{}, terminated correctly",
                        sessionId, consultationSession, userName);

            } else {

                AXSaludUserRoles userRole;
                try {
                    userRole = AXSaludUserRoles.valueOf(role);
                } catch (IllegalArgumentException e) {
                    log.error("{}_ Invalid role '{}' for user '{}'", sessionId, role, userName, e);
                    return;
                }

                if (ConsultationSessionStatus.WAITING_FOR_DOCTOR.equals(status)) {
                    log.info("{}_ consultationSessionID: {} for user:{}, consultation dropped from" +
                                    " waiting for a doctor, calling handled session abandoned, role logic added",
                            sessionId, consultationSession.getConsultationSessionId(), userName);
                    ConsultationEventDTO consultationEventDTO = null;

                    try {
                        consultationEventDTO = handledSessionAbandoned(sessionId, consultationSession,
                                ConsultationSessionStatus.WAITING_FROM_DOCTOR_ABANDONED, ConsultationStatus.ABANDONED,
                                userRole, userName);
                        consultationEventDTO.setTransportSessionId(sessionId);
                        appOutboundService.sendDoctorEventMessage(consultationEventDTO);
                        sendConsultationEvent(sessionId, consultationEventDTO);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (ConsultationSessionStatus.CONNECTING.equals(status)) {
                    log.info("{}_ consultationSessionID: {} for user:{}, consultation dropped from" +
                                    " CONNECTING state",
                            sessionId, consultationSession, userName);
                    if(AXSaludUserRoles.USER.getRole().equalsIgnoreCase(role)) {
                        log.info("{}_ user drop the connection, sending consultation to Abandoned", sessionId);
                        sendConsultationEvent(sessionId, handledSessionAbandoned(sessionId, consultationSession,
                                ConsultationSessionStatus.CONNECTING_ABANDONED, ConsultationStatus.ABANDONED,
                                userRole, userName));
                    } else {
                        log.info("{}_ DOCTOR drop the connection, sending consultation to WAITING_FOR_DOCTOR",
                                sessionId);

                        sendConsultationEvent(sessionId, handledSessionAbandoned(sessionId, consultationSession,
                                ConsultationSessionStatus.WAITING_FOR_DOCTOR, ConsultationStatus.WAITING_FOR_DOCTOR,
                                userRole, userName));

                        ConsultationDTO consultationDTO =
                                createConsultationDTO(consultation, consultationSession,
                                        consultation.getSymptoms());

                        consultationDTO.setStatus(ConsultationStatus.WAITING_FOR_DOCTOR);
                     //   consultation.setCurrentSessionIdIfExists(consultationSession.getConsultationSessionId().toString());
                     //   consultationRepository.save(consultation);

                        sendConsultationDTOToDoctorEvents(sessionId, consultationDTO);
                    }
                    //todo move the consultation back to WAITING FOR DOCTOR in case doctor was the one dropping the connection


                } else if (ConsultationSessionStatus.CONNECTED.equals(status)) {
                    log.info("{}_ consultationSessionID: {} for user:{}, consultation dropped from" +
                                    " CONNECTED state",
                            sessionId, consultationSession, userName);

                    if(AXSaludUserRoles.USER.getRole().equalsIgnoreCase(role)) {

                        sendConsultationEvent(sessionId, handledSessionAbandoned(sessionId, consultationSession,
                                ConsultationSessionStatus.CONNECTED_ABANDONED, ConsultationStatus.ABANDONED,
                                userRole, userName));
                    } else {
                        log.info("{}_ DOCTOR drop the connection, sending consultation to WAITING_FOR_DOCTOR",
                                sessionId);

                        sendConsultationEvent(sessionId, handledSessionAbandoned(sessionId, consultationSession,
                                ConsultationSessionStatus.WAITING_FOR_DOCTOR, ConsultationStatus.WAITING_FOR_DOCTOR,
                                userRole, userName));

                        ConsultationDTO consultationDTO =
                                createConsultationDTO(consultation, consultationSession,
                                        consultation.getSymptoms());

                        consultationDTO.setStatus(ConsultationStatus.WAITING_FOR_DOCTOR);
                     //   consultation.setCurrentSessionIdIfExists(consultationSession.getConsultationSessionId().toString());
                      //  consultationRepository.save(consultation);

                        sendConsultationDTOToDoctorEvents(sessionId, consultationDTO);
                    }
                } else if (ConsultationSessionStatus.CONFIRMING_PARTIES.equals(status)) {
                    log.info("{}_ consultationSessionID: {} for user:{}, consultation dropped from: {}" +
                                    " CONFIRMING_PARTIES state",
                            sessionId, consultationSession, userName, role);

                    if(AXSaludUserRoles.USER.getRole().equalsIgnoreCase(role)) {
                        log.info("{}_ user drop the connection, sending consultation to Abandoned", sessionId);
                        consultationSessionRepository.updateStatus(consultationSessionUUID,
                                ConsultationSessionStatus.CONFIRMING_PARTIES_ABANDONED);
                        sendConsultationEvent(sessionId, handledSessionAbandoned(sessionId, consultationSession,
                                ConsultationSessionStatus.CONFIRMING_PARTIES_ABANDONED, ConsultationStatus.ABANDONED,
                                userRole, userName));
                    } else {
                        log.info("{}_ DOCTOR drop the connection, sending consultation to WAITING_FOR_DOCTOR",
                                sessionId);

                        sendConsultationEvent(sessionId, handledSessionAbandoned(sessionId, consultationSession,
                                ConsultationSessionStatus.WAITING_FOR_DOCTOR, ConsultationStatus.WAITING_FOR_DOCTOR,
                                userRole, userName));

                        ConsultationDTO consultationDTO =
                                createConsultationDTO(consultation, consultationSession,
                                        consultation.getSymptoms());

                        consultationDTO.setStatus(ConsultationStatus.WAITING_FOR_DOCTOR);
                       // consultation.setCurrentSessionIdIfExists(consultationSession.getConsultationSessionId().toString());
                       // consultationRepository.save(consultation);

                        sendConsultationDTOToDoctorEvents(sessionId, consultationDTO);
                    }

                    //todo move the consultation back to WAITING FOR DOCTOR in case doctor was the one dropping the connection
                } else {
                    log.info("{}_ consultationSessionID: {} for user:{}, consultation dropped from" +
                                    " status could not be processed state: {}",
                            sessionId, consultationSession, userName, status);
                }

            }
        }

    }


    @Override
    public ConsultationEventDTO<SessionAbandonedDTO>
    handledSessionAbandoned(final String transportSessionId, final ConsultationSession consultationSession,
                            final ConsultationSessionStatus status, final ConsultationStatus consultationStatus,
                            final AXSaludUserRoles role, final String userName) {

        String doctorName = consultationSession.getDoctor()== null ? "DOCTOR_UNASSIGNED" :
                 consultationSession.getDoctor().getCoreUser().getUserName();
        log.info("{}_ SessionAbandoned, sessionID: {}, role: {}, doctor: {}, patient: {}",
                transportSessionId, consultationSession.getConsultationSessionId(),
                role, doctorName,
                consultationSession.getConsultation().getPatient().getCoreUser().getUserName());

        SessionAbandonedDTO sessionAbandonedDTO = new SessionAbandonedDTO();
        sessionAbandonedDTO.setNewConsultationSessionStatus(ConsultationSessionStatus.ABANDONED.getStatus());
        sessionAbandonedDTO.setConsultationSessionId(consultationSession.getConsultationSessionId().toString());
        sessionAbandonedDTO.setConsultationId(consultationSession.getConsultation().getConsultationId().toString());
        sessionAbandonedDTO.setCurrentState(consultationSession.getStatus().getStatus());

        if(!"DOCTOR_UNASSIGNED".equalsIgnoreCase(doctorName) && role == AXSaludUserRoles.DOCTOR) {
            log.info("{}_ updating doctor state to dropped consultationSessionID:" +
                            " {} for user:{}",
                    transportSessionId, consultationSession.getConsultationSessionId(), userName);
            consultationSessionRepository.updateDoctorStatus(consultationSession.getConsultationSessionId(),
                    PartyConsultationStatus.DROPPED);
            sessionAbandonedDTO.setRole(AXSaludUserRoles.DOCTOR.getRole());
            sessionAbandonedDTO.setUserName(consultationSession.getDoctor()
                    .getCoreUser().getUserName());
            sessionAbandonedDTO.setLastTimeSeen(consultationSession.getDoctorLastTimePing());
        } else {
            log.info("{}_ updating patient state to dropped consultationSessionID:" +
                            " {} for user:{}",
                    transportSessionId, consultationSession.getConsultationSessionId(), userName);
            consultationSessionRepository.updatePatientStatus(consultationSession.getConsultationSessionId(),
                    PartyConsultationStatus.DROPPED);
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
        log.info("{}_ Session abandoned event: {}", transportSessionId, sessionAbandonedDTO);
        consultationSessionRepository.updateStatus(consultationSession.getConsultationSessionId(), status);
        consultationRepository.updateStatus(consultationSession.getConsultation().getConsultationId(), consultationStatus);

        return consultationEventDTO;
    }

    @Override
    public String sendConsultationEvent(final String transportSessionId, ConsultationEventDTO<SessionAbandonedDTO> consultationEventDTO) {
        SessionAbandonedDTO sessionAbandonedDTO = consultationEventDTO.getPayload();
        /*String controlCommunicationTopic = "/topic/consultation." + sessionAbandonedDTO.getConsultationId() +
                ".session." + sessionAbandonedDTO.getConsultationSessionId() + ".control";*/
        log.info("{}_ Sending end session", transportSessionId);
        return appOutboundService.sendSessionAbandonedConsultationControlEvent(transportSessionId,  sessionAbandonedDTO.getConsultationId(),
                sessionAbandonedDTO.getConsultationSessionId(), consultationEventDTO);
        // messagingTemplate.convertAndSend(controlCommunicationTopic, consultationEventDTO);

    }

}
