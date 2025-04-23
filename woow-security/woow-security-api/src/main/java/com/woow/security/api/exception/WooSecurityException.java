package com.woow.security.api.exception;

import lombok.Data;

@Data
public class WooSecurityException extends Exception {
    private String message;
    private int code;

    public WooSecurityException(final String message, final int code) {
        super(message);
        this.message = message;
        this.code = code;
    }
}
