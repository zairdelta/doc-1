package com.woow.serviceprovider.api;

import java.util.List;

public interface ServiceProviderClient {
    TelemedicineResponse isHIDValid(final ServiceProviderRequestDTO serviceProviderRequestDTO,
                                    final String hid) throws ServiceProviderClientException;
    List<TelemedicineAllUsersDTO> getAllUsers();
}
