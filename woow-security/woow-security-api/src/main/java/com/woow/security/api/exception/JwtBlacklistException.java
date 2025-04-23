package com.woow.security.api.exception;

import lombok.Data;

@Data
public class JwtBlacklistException extends Exception {

    String message = "";

    public JwtBlacklistException(String msg) {
        super(msg);
        message = msg;
    }
}
