package com.woow.axsalud.controller;

import com.woow.axsalud.controller.exception.WooBoHttpError;
import com.woow.axsalud.service.api.HealthServiceProvider;
import com.woow.axsalud.service.api.dto.HealthServiceProviderDTO;
import com.woow.core.service.api.exception.WooUserServiceException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/health_provider")
@Validated
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "User Endpoint", description = "Operations related to health service providers")
public class HealthServiceProviderController {

    private static final String ROOT_PATH = "/api/woo_user/";
    private static final String LOCATION = "Location";

    private final HealthServiceProvider healthServiceProvider;

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
    @PostMapping("/new")
    @Operation(summary = "Create new Health Service Provider",
            description = "Creates a new Health Service Provider user with the role HEALTH_SERVICE_PROVIDER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User created successfully. Returns Location header."),
            @ApiResponse(responseCode = "301", description = "Forbidden"),
            @ApiResponse(responseCode = "410", description = "Password cannot be empty"),
            @ApiResponse(responseCode = "411", description = "Username already exists"),
            @ApiResponse(responseCode = "414", description = "Sponsor is not active or does not exist"),
            @ApiResponse(responseCode = "415", description = "User is not active")
    })
    public ResponseEntity<?> save(HttpServletRequest request,
                                  @Valid @RequestBody HealthServiceProviderDTO healthServiceProviderDTO) {
        String userName = "";
        try {
            // captchaService.processResponse(request, gRecaptchaResponse);
            userName = healthServiceProvider.save(healthServiceProviderDTO);
        } catch (final WooUserServiceException e) {
            return WooBoHttpError.of(e).toResponseEntity();
        } catch (Exception e) {
            log.error("Error while creating user: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(LOCATION, appRoot + ROOT_PATH + userName)
                .build();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @GetMapping
    @Operation(summary = "get Health Service User",
            description = "Creates a new Health Service user with the role HEALTH_SERVICE_PROVIDER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Health user details"),
            @ApiResponse(responseCode = "301", description = "Forbidden"),
            @ApiResponse(responseCode = "410", description = "Password cannot be empty"),
            @ApiResponse(responseCode = "411", description = "Username already exists"),
            @ApiResponse(responseCode = "414", description = "Sponsor is not active or does not exist"),
            @ApiResponse(responseCode = "415", description = "User is not active")
    })
    public ResponseEntity<?> get(HttpServletRequest request,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // captchaService.processResponse(request, gRecaptchaResponse);
            return ResponseEntity.ok(healthServiceProvider.get(userDetails.getUsername()));
        } catch (final WooUserServiceException e) {
            return WooBoHttpError.of(e).toResponseEntity();
        } catch (Exception e) {
            log.error("Error while creating user: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
}
