package com.woow.axsalud.controller;

import com.woow.axsalud.controller.exception.WooBoHttpError;
import com.woow.axsalud.data.repository.PatientConsultationSummary;
import com.woow.axsalud.service.api.AxSaludService;
import com.woow.axsalud.service.api.dto.*;
import com.woow.core.service.api.exception.WooUserServiceException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

import java.util.List;

@RestController
@RequestMapping("/api/woo_user")
@Validated
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Patient User Endpoint", description = "Operations related to patient users")
public class WoowUserController {

    private static final String ROOT_PATH = "/api/woo_user/";
    private static final String LOCATION = "Location";

    private final AxSaludService axSaludService;

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
    @PostMapping("/new")
    @Operation(summary = "Create new Woow User",
            description = "Creates a new Woow User which role will be 'user'. Patientn")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User Created successfully, returns location header"),
            @ApiResponse(responseCode = "301", description = "Forbidden"),
            @ApiResponse(responseCode = "410", description = "Password cannot be empty"),
            @ApiResponse(responseCode = "411", description = "User with username given already exists"),
            @ApiResponse(responseCode = "414", description = "Sponsor is not active or does not exist."),
            @ApiResponse(responseCode = "415", description = "User is not an active user")
    })
    public ResponseEntity<Void> save(HttpServletRequest request,
                                     @Valid @RequestBody AxSaludUserDTO axSaludUserDTO) {
        String userName;
        try {
            userName = axSaludService.save(axSaludUserDTO);
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

    @GetMapping("/history")
    @Operation(
            summary = "Get the user's consultations (Patient)",
            description = "Fetch all consultations for the authenticated user. User must have the PATIENT or USER role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of consultations retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ConsultationDTO.class))
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<PatientConsultationSummary>> getUserConsultations(@AuthenticationPrincipal UserDetails userDetails,
                                                                                 @RequestParam int pageNumber, @RequestParam int elementsPerPage) {
        try {
            return ResponseEntity.ok(axSaludService.getUserHistory(userDetails.getUsername(), pageNumber, elementsPerPage));
        } catch (Exception e) {
            log.error("Error while getting user data: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping
    @Operation(summary = "Get patient information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient found"),
            @ApiResponse(responseCode = "301", description = "Forbidden"),
            @ApiResponse(responseCode = "410", description = "Password cannot be empty"),
            @ApiResponse(responseCode = "411", description = "User with username given already exists"),
            @ApiResponse(responseCode = "414", description = "Sponsor is not active or does not exist."),
            @ApiResponse(responseCode = "415", description = "User is not an active user")
    })
    public ResponseEntity<PatientViewDTO> getPatientData(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(axSaludService.get(userDetails.getUsername()));
        } catch (WooUserServiceException e) {
            log.error("Error while getting user data: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/patientData")
    @Operation(summary = "Add patient data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient data updated"),
            @ApiResponse(responseCode = "301", description = "Forbidden")
    })
    public ResponseEntity<Void> addPatientData(@AuthenticationPrincipal UserDetails userDetails,
                                               @RequestBody PatientDataDTO patientDataDTO) {
        try {
            log.debug("update patient data received, patientData:{}", patientDataDTO);
            axSaludService.updatePatientData(userDetails.getUsername(), patientDataDTO);
            return ResponseEntity.ok().build();
        } catch (WooUserServiceException e) {
            log.error("Error while updating patient data: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping
    @Operation(summary = "Updates a Patient",
            description = "Updates a Patient, the new information will be replace, endpoints except all the fields even if they are the" +
                    "same value.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully. Returns Location header."),
            @ApiResponse(responseCode = "301", description = "Forbidden"),
            @ApiResponse(responseCode = "415", description = "User is not active")
    })
    public ResponseEntity<?> put(@Valid @RequestBody AxSaludUserUpdateDTO axSaludUserUpdateDTO,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        String userName = userDetails.getUsername();
        try {
            userName = axSaludService.update(userName, axSaludUserUpdateDTO);
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

    @GetMapping("/docPrescriptions")
    @Operation(summary = "Get patient Prescriptions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient found"),
            @ApiResponse(responseCode = "301", description = "Forbidden")
    })
    public ResponseEntity<List<DoctorPrescriptionViewDTO>>
    getPatientPrescriptions(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(axSaludService.getDoctorPrescriptions(userDetails.getUsername()));
        } catch (WooUserServiceException e) {
            log.error("Error while getting user data: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/labPrescription")
    @Operation(summary = "Get patient labPrescription")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient found"),
            @ApiResponse(responseCode = "301", description = "Forbidden")
    })
    public ResponseEntity<List<LabPrescriptionViewDTO>>
    getLabPrescriptions(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(axSaludService.getLabPrescriptions(userDetails.getUsername()));
        } catch (WooUserServiceException e) {
            log.error("Error while getting user data: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }



}
