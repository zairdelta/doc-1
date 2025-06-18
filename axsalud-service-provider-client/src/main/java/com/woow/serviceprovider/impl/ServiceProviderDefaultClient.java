package com.woow.serviceprovider.impl;

import com.woow.serviceprovider.api.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("DEFAULT")
@Slf4j
public class ServiceProviderDefaultClient implements ServiceProviderClient {
    @Override
    public TelemedicineResponse isHIDValid(ServiceProviderRequestDTO serviceProviderRequestDTO, String hid)
            throws ServiceProviderClientException {
        TelemedicineResponse telemedicineResponse = new TelemedicineResponse();
        telemedicineResponse.setCode(200);
        return telemedicineResponse;
    }

    @Override
    public List<TelemedicineAllUsersDTO> getAllUsers() {
        return new ArrayList<>();
    }
}
