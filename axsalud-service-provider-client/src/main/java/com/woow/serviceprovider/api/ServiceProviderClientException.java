package com.woow.serviceprovider.api;

import com.woow.core.service.api.exception.WooBoException;

public class ServiceProviderClientException extends WooBoException {
    public ServiceProviderClientException(String msg, int code) {
        super(msg, code);
    }
}
