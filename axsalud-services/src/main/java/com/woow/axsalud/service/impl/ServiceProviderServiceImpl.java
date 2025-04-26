package com.woow.axsalud.service.impl;

import com.woow.axsalud.data.repository.ServiceProviderRepository;
import com.woow.axsalud.data.serviceprovider.ServiceProvider;
import com.woow.axsalud.service.api.ServiceProviderService;
import com.woow.axsalud.service.api.dto.ServiceProviderDTO;
import com.woow.core.service.api.exception.WooUserServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public List<ServiceProviderDTO> getAllServiceProvider() {

        List<ServiceProviderDTO> serviceProviderDTOS = serviceProviderRepository.findAll().stream()
                .map(serviceProvider -> {
                    ServiceProviderDTO serviceProviderDTO = new ServiceProviderDTO();
                    serviceProviderDTO.setName(serviceProvider.getName());
                    serviceProviderDTO.setId(serviceProvider.getId());
                    return serviceProviderDTO;
                })
                .collect(Collectors.toList());
        return serviceProviderDTOS;
    }
}
