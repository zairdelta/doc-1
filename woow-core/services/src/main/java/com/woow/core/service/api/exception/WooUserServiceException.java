package com.woow.core.service.api.exception;

public class WooUserServiceException extends WooBoException {
    public WooUserServiceException(String msg, int error_code) {
        super(msg, error_code);
    }
}