package com.woow.security.api.exception;

import lombok.Data;

@Data
public class ReCaptchaInvalidException extends Exception {
    private String msg;
    private int code;

    public ReCaptchaInvalidException(String reCaptcha_was_not_successfully_validated, int error_code) {
        super(reCaptcha_was_not_successfully_validated);
        this.msg = reCaptcha_was_not_successfully_validated;
        this.code = error_code;
    }
}
