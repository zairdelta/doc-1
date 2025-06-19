package com.woow.axsalud.service.impl;

import com.woow.axsalud.common.AXSaludUserRoles;
import com.woow.axsalud.data.client.AxSaludWooUser;
import com.woow.axsalud.data.client.PatientAdditional;
import com.woow.axsalud.data.client.PatientData;
import com.woow.axsalud.data.client.WoowUserType;
import com.woow.axsalud.data.consultation.*;
import com.woow.axsalud.data.repository.*;
import com.woow.axsalud.data.serviceprovider.ServiceProvider;
import com.woow.axsalud.service.api.AxSaludService;
import com.woow.axsalud.service.api.ServiceProviderService;
import com.woow.axsalud.service.api.dto.*;
import com.woow.axsalud.service.api.exception.ConsultationServiceException;
import com.woow.axsalud.service.api.messages.ConsultationEventDTO;
import com.woow.axsalud.service.api.messages.ConsultationMessageDTO;
import com.woow.core.data.repository.WoowUserRepository;
import com.woow.core.data.user.WoowUser;
import com.woow.core.service.api.UserDtoCreate;
import com.woow.core.service.api.WooWUserService;
import com.woow.core.service.api.exception.WooUserServiceException;
import com.woow.serviceprovider.api.ServiceProviderClient;
import com.woow.serviceprovider.api.ServiceProviderClientException;
import com.woow.serviceprovider.api.ServiceProviderFactory;
import com.woow.serviceprovider.api.ServiceProviderRequestDTO;
import com.woow.storage.api.StorageService;
import com.woow.storage.api.StorageServiceException;
import com.woow.storage.api.StorageServiceUploadResponseDTO;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class AxSaludServiceImpl implements AxSaludService {

    private AxSaludUserRepository axSaludUserRepository;
    private PatientDataRepository patientDataRepository;
    private ModelMapper modelMapper;
    private WooWUserService wooWUserService;
    private WoowUserRepository woowUserRepository;
    private ServiceProviderService serviceProviderService;
    private DoctorPrescriptionRepository doctorPrescriptionRepository;
    private LaboratoryPrescriptionsRepository laboratoryPrescriptionsRepository;
    private ConsultationRepository consultationRepository;
    private ConsultationSessionRepository consultationSessionRepository;
    private ServiceProviderFactory serviceProviderFactory;
    private StorageService storageService;

    public AxSaludServiceImpl(final AxSaludUserRepository axSaludUserRepository,
                              final ModelMapper modelMapper,
                              final WooWUserService wooWUserService,
                              final WoowUserRepository woowUserRepository,
                              final ServiceProviderService serviceProviderService,
                              final ServiceProviderFactory serviceProviderFactory,
                              final PatientDataRepository patientDataRepository,
                              final DoctorPrescriptionRepository doctorPrescriptionRepository,
                              final LaboratoryPrescriptionsRepository laboratoryPrescriptionsRepository,
                              final ConsultationRepository consultationRepository,
                              final StorageService storageService,
                              final ConsultationSessionRepository consultationSessionRepository) {
        this.axSaludUserRepository = axSaludUserRepository;
        this.modelMapper = modelMapper;
        this.wooWUserService = wooWUserService;
        this.woowUserRepository = woowUserRepository;
        this.serviceProviderService = serviceProviderService;
        this.serviceProviderFactory = serviceProviderFactory;
        this.patientDataRepository = patientDataRepository;
        this.doctorPrescriptionRepository = doctorPrescriptionRepository;
        this.laboratoryPrescriptionsRepository = laboratoryPrescriptionsRepository;
        this.consultationRepository = consultationRepository;
        this.storageService = storageService;
        this.consultationSessionRepository = consultationSessionRepository;
    }
    @Override
    public String save(AxSaludUserDTO axSaludUserDTO)
            throws WooUserServiceException {
        UserDtoCreate userDtoCreate = new UserDtoCreate();
        modelMapper.map(axSaludUserDTO.getUserDtoCreate(), userDtoCreate);
        userDtoCreate.setUserName(axSaludUserDTO.getUserDtoCreate().getEmail());
        wooWUserService.save(userDtoCreate);

        WoowUser woowUser = woowUserRepository
                .findByUserName(userDtoCreate.getUserName());
        woowUser.setUserActive(true);
        wooWUserService.addRoleToUser(woowUser.getUserId(), AXSaludUserRoles.USER.getRole());
        AxSaludWooUser axSaludWooUser = new AxSaludWooUser();
        axSaludWooUser.setCoreUser(woowUser);
        axSaludWooUser.setUserType(WoowUserType.PATIENT);

        ServiceProvider serviceProvider = serviceProviderService.
                validateServiceprovider(userDtoCreate.getServiceProvider());

        ServiceProviderClient serviceProviderClient =
                serviceProviderFactory.get(serviceProvider.getEndpoint());

        ServiceProviderRequestDTO serviceProviderRequestDTO = new ServiceProviderRequestDTO();
        serviceProviderRequestDTO.setServiceName(serviceProvider.getName());
        serviceProviderRequestDTO.setUrl(serviceProvider.getEndpoint());
        serviceProviderRequestDTO.setApiKey(serviceProvider.getApiKey());
        try {
            serviceProviderClient
                    .isHIDValid(serviceProviderRequestDTO, axSaludUserDTO.getHid());
        } catch (ServiceProviderClientException e) {
            log.error("Error while validating hid: {}, serviceName: {}, userName: {}",
                    axSaludUserDTO.getHid(), axSaludUserDTO.getUserDtoCreate().getServiceProvider(),
                    axSaludUserDTO.getUserDtoCreate().getEmail());

            throw new WooUserServiceException("invalid HID:" + axSaludUserDTO.getHid(), 406);
        }
        axSaludWooUser.setServiceProvider(serviceProvider.getId());
        axSaludWooUser.setHid(axSaludUserDTO.getHid());
        axSaludWooUser.setDni(axSaludUserDTO.getDni());
        axSaludUserRepository.save(axSaludWooUser);

        return woowUser.getUserName();
    }

    @Override
    public PatientViewDTO get(String userName) throws WooUserServiceException {

        WoowUser woowUser =
                woowUserRepository.findByUserName(userName);

        if(woowUser == null) {
            throw new WooUserServiceException("User Not found: " + userName, 404);
        }

        Optional<AxSaludWooUser> axSaludWooUserOptional =
                axSaludUserRepository.findByCoreUser_UserId(woowUser.getUserId());

        if(axSaludWooUserOptional.isEmpty()) {
            throw new WooUserServiceException("User Not found, HID: " + userName, 404);
        }

        ConsultationSession consultationSession =
                consultationSessionRepository.findLatestByPatientUsername(userName);

        AxSaludWooUser axSaludWooUser = axSaludWooUserOptional.get();

        PatientViewDTO patientViewDTO = new PatientViewDTO();

        patientViewDTO.setLatestConsultationSessionDTO(
                LatestConsultationSessionDTO.from(consultationSession));

        modelMapper.map(woowUser, patientViewDTO);
        modelMapper.map(axSaludWooUser, patientViewDTO);
        PatientDataDTO patientDataDTO = new PatientDataDTO();

        if(axSaludWooUser.getPatientData() != null) {
            modelMapper.map(axSaludWooUser.getPatientData(), patientDataDTO);
            patientDataDTO.setEmergencyContactName(axSaludWooUser.getPatientData().getEmergencyContactName());
            patientDataDTO.setEmergencyContactNumber(axSaludWooUser.getPatientData().getEmergencyContactNumber());
        }
        patientViewDTO.setEmail(woowUser.getEmail());
        patientViewDTO.setDni(axSaludWooUser.getDni());
        patientViewDTO.setPatientDataDTO(patientDataDTO);


        return patientViewDTO;
    }

    @Override
    public List<LabPrescriptionViewDTO>  getLabPrescriptions(String userName) throws WooUserServiceException {
        log.debug("Getting Laboratory prescriptions for userName: {}", userName);

        List<LaboratoryPrescription> laboratoryPrescriptions =
                laboratoryPrescriptionsRepository.findAllByPatientUserName(userName);

        return laboratoryPrescriptions.stream()
                .filter(dp -> {
                    if (dp.getConsultationSession() == null) {
                        log.warn("Skipping DoctorPrescription with null ConsultationSession");
                        return false;
                    }
                    if (dp.getConsultationSession().getDoctor() == null ||
                            dp.getConsultationSession().getDoctor().getCoreUser() == null) {
                        log.warn("Skipping DoctorPrescription with incomplete doctor information");
                        return false;
                    }
                    return true;
                })
                .map(dp -> {
                    LabPrescriptionViewDTO labPrescriptionViewDTO = new LabPrescriptionViewDTO();
                    DoctorPrescriptionOwnerDTO doctorPrescriptionOwnerDTO = new DoctorPrescriptionOwnerDTO();
                    doctorPrescriptionOwnerDTO.setCreatedAt(dp.getConsultationSession().getCreatedAt());

                    LaboratoryPrescriptionDTO dto = new LaboratoryPrescriptionDTO();
                    dto.setNotasDeRecomendaciones(dp.getNotasDeRecomendaciones());
                    dto.setObservacionesMedicas(dp.getObservacionesMedicas());
                    dto.setPosibleDiagnostico(dp.getPosibleDiagnostico());
                    dto.setOrdenDeLaboratorio(dp.getOrdenDeLaboratorio());

                    labPrescriptionViewDTO.setLaboratoryPrescriptionDTO(dto);


                    AxSaludWooUser doctor = dp.getConsultationSession().getDoctor();
                    WoowUser coreUser = doctor.getCoreUser();

                    doctorPrescriptionOwnerDTO.setDoctorFullName(coreUser.getName() + " " + coreUser.getLastName());
                    doctorPrescriptionOwnerDTO.setDoctorDNI(doctor.getDni());
                    doctorPrescriptionOwnerDTO.setDoctorEmail(coreUser.getEmail());
                    labPrescriptionViewDTO.setDoctorPrescriptionOwnerDTO(doctorPrescriptionOwnerDTO);

                    return labPrescriptionViewDTO;
                })
                .collect(Collectors.toList());
    }
    @Override
    public List<DoctorPrescriptionViewDTO> getDoctorPrescriptions(String userName) {
        log.debug("Getting Doctor prescriptions for userName: {}", userName);

        List<DoctorPrescription> doctorPrescriptions =
                doctorPrescriptionRepository.findAllByPatientUserName(userName);

        return doctorPrescriptions.stream()
                .filter(dp -> {
                    if (dp.getConsultationSession() == null) {
                        log.warn("Skipping DoctorPrescription with null ConsultationSession");
                        return false;
                    }
                    if (dp.getConsultationSession().getDoctor() == null ||
                            dp.getConsultationSession().getDoctor().getCoreUser() == null) {
                        log.warn("Skipping DoctorPrescription with incomplete doctor information");
                        return false;
                    }
                    return true;
                })
                .map(dp -> {
                    DoctorPrescriptionViewDTO viewDTO = new DoctorPrescriptionViewDTO();
                    DoctorPrescriptionOwnerDTO doctorPrescriptionOwnerDTO = new DoctorPrescriptionOwnerDTO();
                    doctorPrescriptionOwnerDTO.setCreatedAt(dp.getConsultationSession().getCreatedAt());

                    DoctorPrescriptionDTO dto = new DoctorPrescriptionDTO();
                    dto.setDiagnostico(dp.getDiagnostico());
                    dto.setRecetaMedica(dp.getRecetaMedica());
                    dto.setNotasDeRecomendaciones(dp.getNotasDeRecomendaciones());

                    if (dp.getConsultationSession().getComentariosMedicos() != null) {
                        dto.setComentariosMedicos(
                                dp.getConsultationSession().getComentariosMedicos().getObservacionesMedicas()
                        );
                    }

                    viewDTO.setDoctorPrescriptionDTO(dto);

                    AxSaludWooUser doctor = dp.getConsultationSession().getDoctor();
                    WoowUser coreUser = doctor.getCoreUser();

                    doctorPrescriptionOwnerDTO.setDoctorFullName(coreUser.getName() + " " + coreUser.getLastName());
                    doctorPrescriptionOwnerDTO.setDoctorDNI(doctor.getDni());
                    doctorPrescriptionOwnerDTO.setDoctorEmail(coreUser.getEmail());
                    viewDTO.setDoctorPrescriptionOwnerDTO(doctorPrescriptionOwnerDTO);

                    return viewDTO;
                })
                .collect(Collectors.toList());
    }


    @Override
    public List<ConsultationDTO> getConsultation(String userName) throws WooUserServiceException {

        WoowUser woowUser =
                woowUserRepository.findByUserName(userName);

        if(woowUser == null) {
            throw new WooUserServiceException("User Not found: " + userName, 404);
        }

        Optional<AxSaludWooUser> axSaludWooUserOptional =
                axSaludUserRepository.findByCoreUser_UserId(woowUser.getUserId());

        if(axSaludWooUserOptional.isEmpty()) {
            throw new WooUserServiceException("User Not found, HID: " + userName, 404);
        }

        AxSaludWooUser axSaludWooUser = axSaludWooUserOptional.get();
        List<Consultation> consultations =
                axSaludWooUser.getPatientConsultations();

        List<ConsultationDTO> consultationDTOS = new ArrayList<>();

        for(Consultation consultation:consultations) {
            ConsultationDTO consultationDTO =  ConsultationDTO.from(consultation);
            /*consultationDTO.setConsultationId(consultation.getConsultationId().toString());
            consultationDTO.setId(consultation.getId());
            consultationDTO.setConsultationId(consultation.getConsultationId().toString());
            consultationDTO.setCreatedAt(consultation.getCreatedAt());
            consultationDTO.setFinishedAt(consultation.getFinishedAt());
            consultationDTO.setSymptoms(consultation.getSymptoms());

            List<ConsultationSessionIdDTO> consultationSessionIdDTOList = new ArrayList<>();

            for(ConsultationSession consultationSession:consultation.getSessions()) {
                ConsultationSessionIdDTO consultationSessionIdDTO = new ConsultationSessionIdDTO();
                consultationSessionIdDTO.setStartAt(consultationSession.getStartAt());
                consultationSessionIdDTO
                        .setConsultationSessionId(consultationSession.getConsultationSessionId().toString());
                consultationSessionIdDTO.setFinishedAt(consultationSession.getFinishedAt());

                if(consultationSession.getDoctor() != null) {
                    DoctorViewDTO doctorViewDTO = new DoctorViewDTO();
                    AxSaludWooUser axDoctor = consultationSession.getDoctor();
                    WoowUser doctorCore = axDoctor.getCoreUser();
                    doctorViewDTO.setLastName(doctorCore.getLastName());
                    doctorViewDTO.setCp(doctorCore.getCp());
                    doctorViewDTO.setName(doctorCore.getName());
                    doctorViewDTO.setDni(axDoctor.getDni());

                    DoctorDataDTO doctorDataDTO = new DoctorDataDTO();
                    doctorDataDTO.setUniversity(axDoctor.getDoctorData().getUniversity());
                    doctorDataDTO.setMatriculaNacional(axDoctor.getDoctorData().getMatriculaNacional());
                    doctorDataDTO.setSpeciality(axDoctor.getDoctorData().getSpeciality());
                    doctorViewDTO.setDoctorData(doctorDataDTO);

                    consultationSessionIdDTO.setDoctorViewDTO(doctorViewDTO);
                }
                consultationSessionIdDTOList.add(consultationSessionIdDTO);
            }
            consultationDTO.setConsultationSessionIdDTOList(consultationSessionIdDTOList);
*/
            consultationDTOS.add(consultationDTO);
        }

        return consultationDTOS;

    }

    @Override
    public void updatePatientData(String userName,
                                  PatientDataDTO patientDataDTO)
            throws WooUserServiceException {

        log.info("updating patientData: {}, userName: {}", patientDataDTO, userName);
        if(patientDataDTO != null) {

            log.info("updating patient data, patientData is present");
            WoowUser woowUser =
                    woowUserRepository.findByUserName(userName);

            if (woowUser == null) {
                throw new WooUserServiceException("User Not found: " + userName, 404);
            }

            Optional<AxSaludWooUser> axSaludWooUserOptional =
                    axSaludUserRepository.findByCoreUser_UserId(woowUser.getUserId());

            if (axSaludWooUserOptional.isEmpty()) {
                throw new WooUserServiceException("User Not found, HID: " + userName, 404);
            }

            AxSaludWooUser axSaludWooUser = axSaludWooUserOptional.get();

            PatientData existingPatientData = axSaludWooUser.getPatientData();

            if (existingPatientData == null) {
                PatientData newPatientData = new PatientData();
                modelMapper.map(patientDataDTO, newPatientData);
                axSaludWooUser.setPatientData(newPatientData);
            } else {
                modelMapper.typeMap(PatientDataDTO.class, PatientData.class)
                        .addMappings(mapper -> mapper.skip(PatientData::setPatientAdditionalSet));
                modelMapper.map(patientDataDTO, existingPatientData);
            }


            Set<PatientAdditional> patientAdditionals =
                    patientDataDTO.getPatientAdditionalSet()
                            .stream()
                            .map(PatientAdditionalDTO::from)
                            .collect(Collectors.toSet());

            axSaludWooUser.getPatientData().getPatientAdditionalSet().clear();

            axSaludWooUser
                    .getPatientData()
                    .getPatientAdditionalSet().addAll(patientAdditionals);


            axSaludUserRepository.save(axSaludWooUser);
            log.debug("patientData was updated for: {}...", userName);
        }
    }

    @Override
    public String update(String userName, AxSaludUserUpdateDTO axSaludUserUpdateDTO) throws WooUserServiceException {
        log.debug("Patient userName to be updated: {}, new Data: {}", userName, axSaludUserUpdateDTO);
        WoowUser woowUser = wooWUserService.updateWooUserByUserName(userName, axSaludUserUpdateDTO.getUserUpdateDto());
        Optional<AxSaludWooUser> axSaludWooUserOptional =
                axSaludUserRepository.findByCoreUser_UserId(woowUser.getUserId());
        AxSaludWooUser axSaludWooUser =
                axSaludWooUserOptional.orElseThrow(() -> new WooUserServiceException("Error trying to update user: " +
                        userName, 402));

        axSaludWooUser.setDni(axSaludUserUpdateDTO.getDni());

        updatePatientData(userName,axSaludUserUpdateDTO
                .getPatientDataUpdateDTO().getPatientDataDTO());

        axSaludUserRepository.save(axSaludWooUser);
        return userName;
    }

    @Override
    public List<PatientConsultationSummary> getUserHistory(String userName, int pageNumber,
                                                           int totalElementsPerPage) {

        log.info("getting history for patient: {}, pagenumber: {} , total Elements: {}", userName, pageNumber, totalElementsPerPage);
        Pageable pageable = PageRequest.of(pageNumber, totalElementsPerPage);

        WoowUser woowUser = woowUserRepository.findByUserName(userName);

        Optional<AxSaludWooUser>
                axSaludWooUserOptional = axSaludUserRepository.findByCoreUser_UserId(woowUser.getUserId());

        if(!axSaludWooUserOptional.isEmpty()) {
            AxSaludWooUser axSaludWooUser = axSaludWooUserOptional.get();

            log.info("Getting history for userId: {}, userName:{} ", axSaludWooUser.getId(), userName);

            Page<PatientConsultationSummary> consultations = consultationRepository
                    .findConsultationsByPatientId(axSaludWooUser.getId(), pageable);
            List<PatientConsultationSummary> patientConsultationSummaryList = consultations.getContent();
            log.info("History content size: {}", patientConsultationSummaryList.size());
            return patientConsultationSummaryList;
        } else {
            log.info("user not found in the system: {}, userId: {}", userName, woowUser.getUserId());

            return new ArrayList<>();
        }
    }

    @Override
    public FileResponseDTO appendDocument(String userName, MultipartFile file) throws WooUserServiceException {
        try {
            WoowUser woowUser = woowUserRepository.findByUserName(userName);
            Optional<AxSaludWooUser> axSaludWooUserOptional =
                    axSaludUserRepository.findByCoreUser_UserId(woowUser.getUserId());

            AxSaludWooUser axSaludWooUser = axSaludWooUserOptional.get();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(file.getInputStream())
                    .size(200, 200)
                    .outputFormat("jpeg")
                    .toOutputStream(outputStream);

            byte[] resizedImageBytes = outputStream.toByteArray();

            MultipartFile resizedFile = new InMemoryMultipartFile(
                    file.getName(),
                    file.getOriginalFilename(),
                    "image/jpeg",
                    resizedImageBytes
            );


            StorageServiceUploadResponseDTO storageServiceUploadResponseDTO =
                    storageService.uploadFile(resizedFile);

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

            FileResponseDTO fileResponseDTO = new FileResponseDTO();
            fileResponseDTO.setName(doc.getFileName());
            fileResponseDTO.setUrl(doc.getSecureUrl());
            woowUser.setImgURL(doc.getSecureUrl());

            return fileResponseDTO;
        } catch (StorageServiceException e) {
            throw new WooUserServiceException(e.getMessage(), 301);
        } catch (IOException e) {
            log.error("Error in Resizing file:{} ", e);
            throw new WooUserServiceException("Failing resizing file", 301);
        }
    }

}
