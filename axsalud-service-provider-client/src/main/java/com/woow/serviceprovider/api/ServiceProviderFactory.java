package com.woow.serviceprovider.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ServiceProviderFactory {

    private final ServiceProviderClient localService;
    private final ServiceProviderClient remoteService;
    private final ServiceProviderClient defaultService;

    @Autowired
    public ServiceProviderFactory(
            @Qualifier("LOCAL") ServiceProviderClient localService,
            @Qualifier("REMOTE") ServiceProviderClient remoteService,
            @Qualifier("DEFAULT") ServiceProviderClient defaultService

    ) {
        this.localService = localService;
        this.remoteService = remoteService;
        this.defaultService = defaultService;
    }

    public ServiceProviderClient get(String entpoint) {
        log.info("Getting service provider for: {}", entpoint);
        if (entpoint == null || entpoint.trim().equalsIgnoreCase("LOCAL")) {
            log.info("Service provider Local: {}", localService);
            return localService;
        }

        if (entpoint.toLowerCase().startsWith("http")) {
            log.info("Service provider remote: {}", remoteService);
            return remoteService;
        }

        log.info("Service provider default: {}", defaultService);
        return defaultService;
    }
}