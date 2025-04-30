package com.woow.axsalud.service.api.exception;

import com.woow.core.service.api.exception.WooBoException;

public class WoowVideoCallException extends WooBoException {
    public WoowVideoCallException(String msg, int code) {
        super(msg, code);
    }
}
