package com.woow.axsalud.service.impl;

import com.woow.axsalud.data.repository.ServiceProviderRepository;
import com.woow.axsalud.data.serviceprovider.ServiceProvider;
import com.woow.axsalud.service.api.ServiceProviderService;
import com.woow.core.service.api.exception.WooUserServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
@Transactional
public class ServiceProviderServiceImpl implements ServiceProviderService {
    private ServiceProviderRepository serviceProviderRepository;

    public ServiceProviderServiceImpl(ServiceProviderRepository serviceProviderRepository) {
        this.serviceProviderRepository = serviceProviderRepository;
    }
    @Override
    public ServiceProvider validateServiceprovider(String name) throws WooUserServiceException {
        if(ObjectUtils.isEmpty(name)) {
            throw new WooUserServiceException("Service Provider cannot be empty", 402);
        }

        ServiceProvider serviceProvider =
                serviceProviderRepository.findByName(name);

        if(serviceProvider == null) {
            throw new WooUserServiceException("Service Provider not found", 404);

        }

        return serviceProvider;
    }
}
