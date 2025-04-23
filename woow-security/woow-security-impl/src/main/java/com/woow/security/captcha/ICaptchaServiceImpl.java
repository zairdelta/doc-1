package com.woow.security.captcha;

import com.woow.security.config.CaptchaSettings;
import com.woow.security.api.GoogleResponse;
import com.woow.security.api.ICaptchaService;
import com.woow.security.api.exception.InvalidReCaptchaException;
import com.woow.security.api.exception.ReCaptchaInvalidException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;

import java.net.URI;
import java.util.regex.Pattern;

@Slf4j
@Service
@Profile({"dev", "integration", "prod", "staging"})
public class ICaptchaServiceImpl implements ICaptchaService {
    @Autowired
    private CaptchaSettings captchaSettings;

    @Autowired
    private ReCaptchaAttemptServiceImpl reCaptchaAttemptService;

    @Autowired
    private RestOperations restTemplate;

    private static Pattern RESPONSE_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");


    @Override
    public void processResponse (HttpServletRequest request, String gRecaptchaResponse) throws InvalidReCaptchaException, ReCaptchaInvalidException {

        String clientIp = getClientIP(request);
        log.info("Recaptcha-ICaptchaServiceImpl-clientIp: " + clientIp);
        log.info("Recaptcha-ICaptchaServiceImpl-gRecaptchaResponse: " + gRecaptchaResponse);

        if(reCaptchaAttemptService.isBlocked(clientIp)) {
            throw new InvalidReCaptchaException("Client exceeded maximum number of failed attempts",429);
        }

        if(!responseSanityCheck(gRecaptchaResponse)) {
            throw new InvalidReCaptchaException("Response contains invalid characters",429);
        }

        System.out.println("getReCaptchaSecret(): " + getReCaptchaSecret());
        URI verifyUri = URI.create(String.format(
                "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s&remoteip=%s",
                "6LeBD2ghAAAAAIwmqN_EjY0H0vUFcoEPH-mwc8IB", gRecaptchaResponse, clientIp));
        GoogleResponse googleResponse = restTemplate.getForObject(verifyUri, GoogleResponse.class);

        System.out.println(googleResponse);

        if(!googleResponse.isSuccess()) {
            if(googleResponse.hasClientError()) {
                reCaptchaAttemptService.reCaptchaFailed(clientIp);
            }
            throw new ReCaptchaInvalidException("reCaptcha was not successfully validated: " +
                    googleResponse.getError() ,429);
        }

        if(!googleResponse.isSuccess()) {
            throw new ReCaptchaInvalidException("reCaptcha was not successfully validated: " +
                    googleResponse.getError(),429);
        }
        reCaptchaAttemptService.reCaptchaSucceeded(clientIp);

    }

    private boolean responseSanityCheck(String response) {
        return StringUtils.hasLength(response) && RESPONSE_PATTERN.matcher(response).matches();
    }

    public String getReCaptchaSecret() {
        return captchaSettings.getSecret();
    }

    private String getClientIP(HttpServletRequest request) {
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

}