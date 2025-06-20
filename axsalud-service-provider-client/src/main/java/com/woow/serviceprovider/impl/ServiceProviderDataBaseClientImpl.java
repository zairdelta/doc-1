package com.woow.serviceprovider.impl;

import com.woow.axsalud.data.repository.LocalServiceProviderUserRepository;
import com.woow.axsalud.data.repository.ServiceProviderRepository;
import com.woow.axsalud.data.serviceprovider.LocalServiceProviderUserEntity;
import com.woow.axsalud.data.serviceprovider.ServiceProvider;
import com.woow.serviceprovider.api.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Service("LOCAL")
@Slf4j
public class ServiceProviderDataBaseClientImpl implements ServiceProviderClient {

    private LocalServiceProviderUserRepository localServiceProviderUserRepository;
    private ServiceProviderRepository serviceProviderRepository;
    public ServiceProviderDataBaseClientImpl(final LocalServiceProviderUserRepository localServiceProviderUserRepository,
                                             final ServiceProviderRepository serviceProviderRepository) {
        this.localServiceProviderUserRepository = localServiceProviderUserRepository;
        this.serviceProviderRepository = serviceProviderRepository;
    }
    @Override
    public TelemedicineResponse
    isHIDValid(ServiceProviderRequestDTO serviceProviderRequestDTO, String hid)
            throws ServiceProviderClientException {

        if(ObjectUtils.isEmpty(hid) || ObjectUtils.isEmpty(serviceProviderRequestDTO.getServiceName())) {
            throw new ServiceProviderClientException("Invalid hid or serviceName, cannot be empty: " +
                    serviceProviderRequestDTO.getServiceName() + ", " + hid +
                    ", does not exist", 403);
        }

        ServiceProvider serviceProvider =
                serviceProviderRepository.findByName(serviceProviderRequestDTO.getServiceName());

        if(serviceProvider == null) {
            throw new ServiceProviderClientException("Service Provider: " + serviceProviderRequestDTO.getServiceName() +
                    ", does not exist", 403);
        }

        LocalServiceProviderUserEntity localServiceProviderUserEntity =
                localServiceProviderUserRepository.findByServiceProviderIdAndHid(serviceProvider.getId(), hid);

        TelemedicineResponse telemedicineResponse = new TelemedicineResponse();
        if(localServiceProviderUserEntity != null &&
                localServiceProviderUserEntity.getUserValid() == 1) {
            telemedicineResponse.setCode(200);
        } else {
            telemedicineResponse.setCode(404);
        }

        return telemedicineResponse;
    }

    @Override
    public List<TelemedicineAllUsersDTO> getAllUsers() {
        return new ArrayList<>();
    }
}
