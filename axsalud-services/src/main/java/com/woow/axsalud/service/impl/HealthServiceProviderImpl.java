package com.woow.axsalud.service.impl;

import com.woow.axsalud.common.AXSaludUserRoles;
import com.woow.axsalud.data.client.AxSaludWooUser;
import com.woow.axsalud.data.client.DoctorData;
import com.woow.axsalud.data.client.WoowUserType;
import com.woow.axsalud.data.repository.AxSaludUserRepository;
import com.woow.axsalud.data.repository.DoctorDataRepository;
import com.woow.axsalud.service.api.DoctorQueueManager;
import com.woow.axsalud.service.api.HealthServiceProvider;
import com.woow.axsalud.service.api.dto.HealthServiceProviderDTO;
import com.woow.core.data.repository.WoowUserRepository;
import com.woow.core.data.user.WoowUser;
import com.woow.core.service.api.UserDtoCreate;
import com.woow.core.service.api.WooWUserService;
import com.woow.core.service.api.exception.WooUserServiceException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        healthServiceProviderDTO.getDoctorData().setId(0);
        UserDtoCreate userDtoCreate = new UserDtoCreate();
        modelMapper.map(healthServiceProviderDTO.getUserDtoCreate(), userDtoCreate);
        wooWUserService.save(userDtoCreate);

        WoowUser woowUser = woowUserRepository.findByUserName(userDtoCreate.getUserName());
        wooWUserService.addRoleToUser(woowUser.getUserId(),
                AXSaludUserRoles.DOCTOR.getRole());
        AxSaludWooUser axSaludWooUser = new AxSaludWooUser();
        axSaludWooUser.setCoreUser(woowUser);
        axSaludWooUser.setUserType(WoowUserType.HEALTH_SERVICE_PROVIDER);
        axSaludWooUser.setHid("NA");
        axSaludWooUser.setDoctorWelcomeMessage(healthServiceProviderDTO.getWelcomeMessage());

        if(healthServiceProviderDTO.getDoctorData() != null) {
            DoctorData doctorData = new DoctorData();
            modelMapper.map(healthServiceProviderDTO.getDoctorData(), doctorData);
            doctorDataRepository.save(doctorData);
            axSaludWooUser.setDoctorData(doctorData);
        }

        axSaludUserRepository.save(axSaludWooUser);


        return woowUser.getUserName();
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
