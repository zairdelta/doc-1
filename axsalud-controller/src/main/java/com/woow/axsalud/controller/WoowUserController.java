package com.woow.axsalud.controller;

import com.woow.axsalud.controller.exception.WooBoHttpError;
import com.woow.axsalud.data.client.PatientData;
import com.woow.axsalud.service.api.AxSaludService;
import com.woow.axsalud.service.api.dto.AxSaludUserDTO;
import com.woow.axsalud.service.api.dto.PatientViewDTO;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(tags = "Patient User Endpoint")
@RestController
@RequestMapping("/api/woo_user")
@Validated
@Slf4j
@CrossOrigin(origins = "*")
public class WoowUserController {
    private static final String ROOT_PATH = "/api/woo_user/";
    private static final String LOCATION = "Location";

    private AxSaludService axSaludService;

    @Value("${email.group:noreply@axsalud.io}")
    private String emailGroup;

    @Value("${email.username:noreply@axsalud.io}")
    private String fromUser;

    @Value("${application.host}")
    private String appRoot;

    public WoowUserController(final AxSaludService axSaludService) {

        this.axSaludService = axSaludService;
    }
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Creates a new Woow User which role will be user. Patientn")
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
                               @Valid @RequestBody AxSaludUserDTO axSaludUserDTO) {
        String userName = "";
        try {
            // captchaService.processResponse(request, gRecaptchaResponse);
            userName = axSaludService.save(axSaludUserDTO);
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

    @GetMapping
    @ApiOperation(value = "Gets patient Information")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK - Patinent Found", response = PatientViewDTO.class),
            @ApiResponse(code = 301, message = "forbidden"),
            @ApiResponse(code = 410, message = "password cannot be empty"),
            @ApiResponse(code = 411, message = "User with username given already exist"),
            @ApiResponse(code = 414, message = "Sponsor is not active or does not exist."),
            @ApiResponse(code = 415, message = "User is not an active user"),
    })
    public ResponseEntity<PatientViewDTO> getPatientData(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(axSaludService.get(userDetails.getUsername()));
        } catch (WooUserServiceException e) {
            log.error("Error while getting user Data user: {}", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/patientData")
    @ApiOperation(value = "Add Patient Data Information")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK - Patinent Found"),
            @ApiResponse(code = 301, message = "forbidden")
    })
    public ResponseEntity addPatientData(@AuthenticationPrincipal UserDetails userDetails,
                                         @RequestBody PatientData patientData) {
        try {
            axSaludService.updatePatientData(userDetails.getUsername(), patientData);
            return ResponseEntity.ok().build();
        } catch (WooUserServiceException e) {
            log.error("Error while getting user Data user: {}", e);
            return ResponseEntity.notFound().build();
        }
    }

}
