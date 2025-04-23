package com.woow.axsalud.controller;

import com.woow.axsalud.controller.exception.WooBoHttpError;
import com.woow.axsalud.service.api.HealthServiceProvider;
import com.woow.axsalud.service.api.dto.HealthServiceProviderDTO;
import com.woow.core.service.api.exception.WooUserServiceException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(tags = "User Endpoint")
@RestController
@RequestMapping("/api/health_provider")
@Validated
@Slf4j
@CrossOrigin(origins = "*")
public class HealthServiceProviderController {
    private static final String ROOT_PATH = "/api/woo_user/";
    private static final String LOCATION = "Location";

    private HealthServiceProvider healthServiceProvider;

    @Value("${email.group:noreply@axsalud.io}")
    private String emailGroup;

    @Value("${email.username:noreply@axsalud.io}")
    private String fromUser;

    @Value("${application.host}")
    private String appRoot;

    public HealthServiceProviderController(final HealthServiceProvider healthServiceProvider) {
        this.healthServiceProvider = healthServiceProvider;
    }
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Creates a new Health Service Provider which role will be" +
            " HEALTH_SERVICE_PROVIDER")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK - Found User Created successfully, returns the http location header, indicating the url of the user"),
            @ApiResponse(code = 301, message = "forbidden"),
            @ApiResponse(code = 410, message = "password cannot be empty"),
            @ApiResponse(code = 411, message = "User with username given already exist"),
            @ApiResponse(code = 414, message = "Sponsor is not active or does not exist."),
            @ApiResponse(code = 415, message = "User is not an active user"),
    })
    @PostMapping("/new")
    public ResponseEntity save(HttpServletRequest request,
                               @Valid @RequestBody
                               HealthServiceProviderDTO healthServiceProviderDTO) {
        String userName = "";
        try {
            // captchaService.processResponse(request, gRecaptchaResponse);
            userName = healthServiceProvider.save(healthServiceProviderDTO);
        } catch (final WooUserServiceException e) {
            return WooBoHttpError
                    .of(e)
                    .toResponseEntity();
        } catch (Exception e) {
            log.error("Error while creating user: {}", e);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(LOCATION, appRoot + ROOT_PATH + userName)
                .build();
    }
}
