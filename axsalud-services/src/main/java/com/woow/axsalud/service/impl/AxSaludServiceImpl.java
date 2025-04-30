package com.woow.axsalud.service.impl;

import com.woow.axsalud.common.AXSaludUserRoles;
import com.woow.axsalud.data.client.AxSaludWooUser;
import com.woow.axsalud.data.client.PatientData;
import com.woow.axsalud.data.client.WoowUserType;
import com.woow.axsalud.data.consultation.Consultation;
import com.woow.axsalud.data.consultation.ConsultationSession;
import com.woow.axsalud.data.repository.AxSaludUserRepository;
import com.woow.axsalud.data.repository.PatientDataRepository;
import com.woow.axsalud.data.serviceprovider.ServiceProvider;
import com.woow.axsalud.service.api.AxSaludService;
import com.woow.axsalud.service.api.ServiceProviderService;
import com.woow.axsalud.service.api.dto.*;
import com.woow.core.data.repository.WoowUserRepository;
import com.woow.core.data.user.WoowUser;
import com.woow.core.service.api.UserDtoCreate;
import com.woow.core.service.api.WooWUserService;
import com.woow.core.service.api.exception.WooUserServiceException;
import com.woow.serviceprovider.api.ServiceProviderClient;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private ServiceProviderClient serviceProviderClient;

    public AxSaludServiceImpl(final AxSaludUserRepository axSaludUserRepository,
                              final ModelMapper modelMapper,
                              final WooWUserService wooWUserService,
                              final WoowUserRepository woowUserRepository,
                              final ServiceProviderService serviceProviderService,
                              final ServiceProviderClient serviceProviderClient,
                              final PatientDataRepository patientDataRepository) {
        this.axSaludUserRepository = axSaludUserRepository;
        this.modelMapper = modelMapper;
        this.wooWUserService = wooWUserService;
        this.woowUserRepository = woowUserRepository;
        this.serviceProviderService = serviceProviderService;
        this.serviceProviderClient = serviceProviderClient;
        this.patientDataRepository = patientDataRepository;
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

        axSaludWooUser.setServiceProvider(serviceProvider.getId());
        axSaludWooUser.setHid(axSaludUserDTO.getHid());
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

        AxSaludWooUser axSaludWooUser = axSaludWooUserOptional.get();

        PatientViewDTO patientViewDTO = new PatientViewDTO();

        modelMapper.map(woowUser, patientViewDTO);
        modelMapper.map(axSaludWooUser, patientViewDTO);
        patientViewDTO.setPatientData(axSaludWooUser.getPatientData());

        return patientViewDTO;
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
            ConsultationDTO consultationDTO = new ConsultationDTO();
            consultationDTO.setConsultationId(consultation.getConsultationId().toString());
            consultationDTO.setId(consultation.getId());
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

                DoctorViewDTO doctorViewDTO = new DoctorViewDTO();
                modelMapper.map(consultationSession.getDoctor(), doctorViewDTO);
                consultationSessionIdDTO.setDoctorViewDTO(doctorViewDTO);
                consultationSessionIdDTOList.add(consultationSessionIdDTO);
            }
            consultationDTO.setConsultationSessionIdDTOList(consultationSessionIdDTOList);

            consultationDTOS.add(consultationDTO);
        }

        return consultationDTOS;

    }

    @Override
    public void updatePatientData(String userName, PatientData patientData)
            throws WooUserServiceException {

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

        patientData.setId(0);
        PatientData patientDataNew = new PatientData();
        modelMapper.map(patientData, patientDataNew);
        log.info("patientData: {}", patientDataNew);
        patientDataRepository.save(patientDataNew);
        axSaludWooUser.setPatientData(patientDataNew);
        axSaludUserRepository.save(axSaludWooUser);
    }

    @Override
    public String update(String userName, AxSaludUserUpdateDTO axSaludUserUpdateDTO) throws WooUserServiceException {
        log.debug("Patient userName to be updated: {}, new Data: {}", userName, axSaludUserUpdateDTO);
        wooWUserService.updateWooUserByUserName(userName, axSaludUserUpdateDTO.getUserUpdateDto());

        WoowUser woowUser = woowUserRepository.findByUserName(userName);
        Optional<AxSaludWooUser> axSaludWooUserOptional =
                axSaludUserRepository.findByCoreUser_UserId(woowUser.getUserId());
        AxSaludWooUser axSaludWooUser =
                axSaludWooUserOptional.orElseThrow(() -> new WooUserServiceException("Error trying to update user: " +
                        userName, 402));

        PatientData patientData = new PatientData();

        modelMapper.map(axSaludUserUpdateDTO.getPatientDataUpdateDTO(), patientData);
        axSaludWooUser.setPatientData(patientData);
        axSaludUserRepository.save(axSaludWooUser);
        return userName;
    }

}
