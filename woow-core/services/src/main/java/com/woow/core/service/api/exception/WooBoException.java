package com.woow.core.service.api.exception;

import lombok.Data;

@Data
public class WooBoException extends Exception {
    private String message;
    private int code;

    public WooBoException() {}
    public WooBoException(String msg, int error_code) {
        this.message = msg;
        this.code = error_code;
    }

    public WooBoException(String message, Throwable cause, int error_code) {
        super(message, cause);
        this.message = message;
        this.code = error_code;
    }
}