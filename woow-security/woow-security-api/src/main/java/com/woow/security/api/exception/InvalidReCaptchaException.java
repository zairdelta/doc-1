package com.woow.security.api.exception;

import lombok.Data;

@Data
public class InvalidReCaptchaException extends RuntimeException {

    private String msg;
    private int code;
    public InvalidReCaptchaException(String response_contains_invalid_characters, int error_code) {
        super(response_contains_invalid_characters);
        this.msg = response_contains_invalid_characters;
        this.code = error_code;
    }
}
