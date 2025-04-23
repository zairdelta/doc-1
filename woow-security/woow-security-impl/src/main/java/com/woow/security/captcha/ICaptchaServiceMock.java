package com.woow.security.captcha;

import com.woow.security.api.ICaptchaService;
import com.woow.security.api.exception.InvalidReCaptchaException;
import com.woow.security.api.exception.ReCaptchaInvalidException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;



@Service
@Profile("test")
public class ICaptchaServiceMock implements ICaptchaService {

    @Override
    public void processResponse(HttpServletRequest request, String gRecaptchaResponse) throws InvalidReCaptchaException, ReCaptchaInvalidException {
         // All good
    }
}
