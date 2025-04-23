package com.woow.security.api;


import com.woow.security.api.exception.InvalidReCaptchaException;
import com.woow.security.api.exception.ReCaptchaInvalidException;
import jakarta.servlet.http.HttpServletRequest;

public interface ICaptchaService {
    void processResponse(HttpServletRequest request,
                         String gRecaptchaResponse) throws InvalidReCaptchaException,
            ReCaptchaInvalidException;
}
