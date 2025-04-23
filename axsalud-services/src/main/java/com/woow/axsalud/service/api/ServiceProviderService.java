package com.woow.axsalud.service.api;

import com.woow.axsalud.data.serviceprovider.ServiceProvider;
import com.woow.core.service.api.exception.WooUserServiceException;

public interface ServiceProviderService {
    ServiceProvider validateServiceprovider(final String name)
            throws WooUserServiceException;
}
