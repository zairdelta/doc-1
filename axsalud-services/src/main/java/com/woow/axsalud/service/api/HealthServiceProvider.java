package com.woow.axsalud.service.api;

import com.woow.axsalud.data.client.AxSaludWooUser;
import com.woow.axsalud.service.api.dto.HealthServiceProviderDTO;
import com.woow.core.service.api.exception.WooUserServiceException;

import java.util.List;

public interface HealthServiceProvider {
    String save(HealthServiceProviderDTO healthServiceProviderDTO) throws WooUserServiceException;

    HealthServiceProviderDTO get(String userName) throws WooUserServiceException;
    void connected(AxSaludWooUser axSaludWooUser);
    void disconnected(AxSaludWooUser axSaludWooUser);
}
