package com.woow.axsalud.service.api.exception;

import com.woow.core.service.api.exception.WooBoException;

public class ConsultationServiceException extends WooBoException {
    public ConsultationServiceException(String msg, int code) {
        super(msg, code);
    }
}
