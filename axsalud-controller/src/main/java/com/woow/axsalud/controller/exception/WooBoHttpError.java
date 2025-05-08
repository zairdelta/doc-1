package com.woow.axsalud.controller.exception;

import com.google.gson.Gson;
import com.woow.security.api.exception.InvalidReCaptchaException;
import com.woow.security.api.exception.ReCaptchaInvalidException;
import com.woow.security.api.exception.WooSecurityException;
import com.woow.core.service.api.exception.WooBoException;
import lombok.Data;
import org.springframework.http.ResponseEntity;

@Data
public class WooBoHttpError {

    private String message = "";
    private int code;


    public String toJson() {
        Gson gson = new Gson();

        return gson.toJson(this);
    }

    public ResponseEntity toResponseEntity () {
        return ResponseEntity.status(code)
                .header("Error", message)
                .body(this.toJson());
    }

    public static WooBoHttpError of(InvalidReCaptchaException e) {
        WooBoHttpError WooBoHttpError = new WooBoHttpError();
        WooBoHttpError.setMessage(e.getMessage());
        WooBoHttpError.setCode(e.getCode());
        return WooBoHttpError;
    }

    public static WooBoHttpError of(Exception e) {
        WooBoHttpError WooBoHttpError = new WooBoHttpError();
        WooBoHttpError.setMessage("Error while running operation");
        WooBoHttpError.setCode(633);
        return WooBoHttpError;
    }

    public static WooBoHttpError of(ReCaptchaInvalidException e) {
        WooBoHttpError WooBoHttpError = new WooBoHttpError();
        WooBoHttpError.setMessage(e.getMessage());
        WooBoHttpError.setCode(e.getCode());
        return WooBoHttpError;
    }

    public static WooBoHttpError of(String messge, int code) {
        WooBoHttpError WooBoHttpError = new WooBoHttpError();
        WooBoHttpError.setMessage(messge);
        WooBoHttpError.setCode(code);
        return WooBoHttpError;
    }

    public static WooBoHttpError of(WooSecurityException wooSecurityException) {
        WooBoHttpError WooBoHttpError = new WooBoHttpError();
        WooBoHttpError.setMessage(wooSecurityException.getMessage());
        WooBoHttpError.setCode(wooSecurityException.getCode());
        return WooBoHttpError;
    }

    public static WooBoHttpError of(WooBoException e) {
        WooBoHttpError WooBoHttpError = new WooBoHttpError();
        WooBoHttpError.setMessage(e.getMessage());
        WooBoHttpError.setCode(e.getCode());
        return WooBoHttpError;
    }
}
