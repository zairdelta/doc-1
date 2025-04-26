package com.woow.axsalud.service.api;

import com.woow.axsalud.data.serviceprovider.ServiceProvider;
import com.woow.axsalud.service.api.dto.ServiceProviderDTO;
import com.woow.core.service.api.exception.WooUserServiceException;

import java.util.List;

public interface ServiceProviderService {
    ServiceProvider validateServiceprovider(final String name)
            throws WooUserServiceException;

    List<ServiceProviderDTO> getAllServiceProvider();
}
