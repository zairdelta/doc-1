package com.woow.axsalud.service.impl;

import com.woow.axsalud.common.AXSaludUserRoles;
import com.woow.axsalud.data.client.AxSaludWooUser;
import com.woow.axsalud.data.client.DoctorData;
import com.woow.axsalud.data.client.WoowUserType;
import com.woow.axsalud.data.repository.AxSaludUserRepository;
import com.woow.axsalud.data.repository.DoctorDataRepository;
import com.woow.axsalud.service.api.DoctorQueueManager;
import com.woow.axsalud.service.api.HealthServiceProvider;
import com.woow.axsalud.service.api.dto.DoctorDataDTO;
import com.woow.axsalud.service.api.dto.HealthServiceProviderDTO;
import com.woow.axsalud.service.api.dto.HealthServiceProviderUpdateDTO;
import com.woow.core.data.repository.WoowUserRepository;
import com.woow.core.data.user.WoowUser;
import com.woow.core.service.api.UserDtoCreate;
import com.woow.core.service.api.WooWUserService;
import com.woow.core.service.api.exception.WooUserServiceException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Slf4j
@Service
public class HealthServiceProviderImpl implements HealthServiceProvider {

    private ModelMapper modelMapper;
    private WooWUserService wooWUserService;
    private WoowUserRepository woowUserRepository;
    private AxSaludUserRepository axSaludUserRepository;
    private DoctorQueueManager doctorQueueManager;
    private DoctorDataRepository doctorDataRepository;

    public HealthServiceProviderImpl(ModelMapper modelMapper,
                                     WooWUserService wooWUserService,
                                     WoowUserRepository woowUserRepository,
                                     AxSaludUserRepository axSaludUserRepository,
                                     DoctorQueueManager doctorQueueManager,
                                     DoctorDataRepository doctorDataRepository) {
        this.modelMapper = modelMapper;
        this.wooWUserService = wooWUserService;
        this.woowUserRepository = woowUserRepository;
        this.axSaludUserRepository = axSaludUserRepository;
        this.doctorQueueManager = doctorQueueManager;
        this.doctorDataRepository = doctorDataRepository;
    }


    @Override
    public String save(HealthServiceProviderDTO healthServiceProviderDTO)
            throws WooUserServiceException {


        UserDtoCreate userDtoCreate = new UserDtoCreate();
        modelMapper.map(healthServiceProviderDTO.getUserDtoCreate(), userDtoCreate);
        userDtoCreate.setUserName(healthServiceProviderDTO.getUserDtoCreate().getEmail());
        wooWUserService.save(userDtoCreate);

        WoowUser woowUser = woowUserRepository.findByUserName(userDtoCreate.getUserName());
        wooWUserService.addRoleToUser(woowUser.getUserId(),
                AXSaludUserRoles.DOCTOR.getRole());
        AxSaludWooUser axSaludWooUser = new AxSaludWooUser();
        axSaludWooUser.setCoreUser(woowUser);
        axSaludWooUser.setUserType(WoowUserType.HEALTH_SERVICE_PROVIDER);
        axSaludWooUser.setHid("NA");
        axSaludWooUser.setDoctorWelcomeMessage(healthServiceProviderDTO.getWelcomeMessage());
        axSaludWooUser.setDni(healthServiceProviderDTO.getDni());
        if(healthServiceProviderDTO.getDoctorData() != null) {
            DoctorData doctorData = new DoctorData();
            doctorData.setUniversity(healthServiceProviderDTO.getDoctorData().getUniversity());
            doctorData.setLicenseNumber(healthServiceProviderDTO.getDoctorData().getLicenseNumber());
            doctorData.setSpeciality(healthServiceProviderDTO.getDoctorData().getSpeciality());
            doctorData.setMatriculaProvincial(healthServiceProviderDTO.getDoctorData().getMatriculaProvincial());
            doctorData.setMatriculaNacional(healthServiceProviderDTO.getDoctorData().getMatriculaNacional());
            doctorDataRepository.save(doctorData);
            axSaludWooUser.setDoctorData(doctorData);
        }

        axSaludUserRepository.save(axSaludWooUser);


        return woowUser.getUserName();
    }

    @Override
    public String update(String userName, HealthServiceProviderUpdateDTO healthServiceProviderDTO)
            throws WooUserServiceException {

        log.debug("Doctor userName to be updated: {}, new Data: {}", userName, healthServiceProviderDTO);
        WoowUser woowUser = wooWUserService.updateWooUserByUserName(userName, healthServiceProviderDTO.getUserUpdateDto());

        Optional<AxSaludWooUser> axSaludWooUserOptional =
                axSaludUserRepository.findByCoreUser_UserId(woowUser.getUserId());
        AxSaludWooUser axSaludWooUser =
                axSaludWooUserOptional.orElseThrow(()
                        -> new WooUserServiceException("Error trying to update user: " +
                        userName, 402));
        axSaludWooUser.setDni(healthServiceProviderDTO.getDni());
        axSaludWooUser.setDoctorWelcomeMessage(healthServiceProviderDTO.getWelcomeMessage());
        DoctorData doctorData = new DoctorData();
        doctorData.setUniversity(healthServiceProviderDTO.getDoctorData().getUniversity());
        doctorData.setLicenseNumber(healthServiceProviderDTO.getDoctorData().getLicenseNumber());
        doctorData.setSpeciality(healthServiceProviderDTO.getDoctorData().getSpeciality());
        doctorData.setMatriculaNacional(healthServiceProviderDTO.getDoctorData().getMatriculaNacional());
        doctorData.setMatriculaProvincial(healthServiceProviderDTO.getDoctorData().getMatriculaProvincial());
        axSaludWooUser.setDoctorData(doctorData);
        return userName;
    }

    @Override
    public HealthServiceProviderDTO get(String userName) throws WooUserServiceException {
        WoowUser woowUser = woowUserRepository.findByUserName(userName);
        if(woowUser == null) {
            throw new WooUserServiceException("userName: " + userName + ", does not exist", 404);
        }
        Optional<AxSaludWooUser> axSaludWooUserOptional =
                axSaludUserRepository.findByCoreUser_UserId(woowUser.getUserId());


        AxSaludWooUser axSaludWooUser = axSaludWooUserOptional
                .orElseThrow(() -> new WooUserServiceException("ax_userName: " + userName +
                ", does not exist", 404));

        HealthServiceProviderDTO dto = new HealthServiceProviderDTO();

        dto.setDoctorData(DoctorDataDTO.from(axSaludWooUser.getDoctorData()));
        dto.setWelcomeMessage(axSaludWooUser.getDoctorWelcomeMessage());
        dto.setDni(axSaludWooUser.getDni());
        UserDtoCreate userDtoCreate = new UserDtoCreate();
        modelMapper.map(woowUser, userDtoCreate);
        dto.setUserDtoCreate(userDtoCreate);

        return dto;
    }

    @Override
    public void connected(AxSaludWooUser axSaludWooUser) {
        doctorQueueManager.doctorConnected(axSaludWooUser.getId());
    }

    @Override
    public void disconnected(AxSaludWooUser axSaludWooUser) {
        doctorQueueManager.doctorDisconnected(axSaludWooUser.getId());
    }
}
