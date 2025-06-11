package com.woow.axsalud.service.api;

import com.woow.axsalud.data.client.AxSaludWooUser;
import com.woow.axsalud.service.api.dto.HealthServiceProviderDTO;
import com.woow.axsalud.service.api.dto.HealthServiceProviderUpdateDTO;
import com.woow.axsalud.service.api.dto.HealthServiceProviderViewDTO;
import com.woow.core.service.api.exception.WooUserServiceException;

public interface HealthServiceProvider {
    String save(HealthServiceProviderDTO healthServiceProviderDTO) throws WooUserServiceException;
    String update(String userName, HealthServiceProviderUpdateDTO healthServiceProviderDTO) throws WooUserServiceException;

    HealthServiceProviderViewDTO get(String userName) throws WooUserServiceException;
    void connected(AxSaludWooUser axSaludWooUser);
    void disconnected(AxSaludWooUser axSaludWooUser);
}
