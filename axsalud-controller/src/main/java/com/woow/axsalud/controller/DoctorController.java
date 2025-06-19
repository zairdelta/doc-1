package com.woow.axsalud.controller;

import com.woow.axsalud.controller.exception.WooBoHttpError;
import com.woow.axsalud.data.repository.PatientConsultationSummary;
import com.woow.axsalud.service.api.AxSaludService;
import com.woow.axsalud.service.api.ConsultationService;
import com.woow.axsalud.service.api.DoctorCommentsService;
import com.woow.axsalud.service.api.dto.*;
import com.woow.axsalud.service.api.exception.ConsultationServiceException;
import com.woow.core.service.api.exception.WooUserServiceException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
@Slf4j
public class DoctorController {

    private     DoctorCommentsService doctorCommentsService;
    private ConsultationService consultationService;
    private AxSaludService axSaludService;
    public DoctorController(final ConsultationService consultationService,
                            final AxSaludService axSaludService,
                            final DoctorCommentsService doctorCommentsService) {
        this.axSaludService = axSaludService;
        this.doctorCommentsService = doctorCommentsService;
        this.consultationService = consultationService;
    }

    @GetMapping("/patient/{userName}/comments")
    @Operation(summary = "Get Patient's comments",
            description = "Retrieve all Doctor's Comments given to a user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieves the Doctor's comments. ROL DOCTOR"),
            @ApiResponse(responseCode = "400", description = "Invalid status parameter"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<DoctorCommentsDTO>>
    getDoctorComment(@PathVariable String userName,
                     @AuthenticationPrincipal UserDetails userDetails) {


        List<DoctorCommentsDTO> doctorCommentsDTOS =
                doctorCommentsService.getDoctorCommentsByUserName(userName);
        return ResponseEntity.ok().body(doctorCommentsDTOS);
    }

    @GetMapping("/patient/consultation/{consultationId}/sessionId/{consultationSessionId}/consultationMessages")
    public ResponseEntity<ConsultationMessagesPagingDTO> getConsultationMessagesBySessionIdAndUserName(
            @PathVariable String consultationId, @PathVariable String consultationSessionId,
            @RequestParam int pageNumber, @RequestParam int elementsPerPage) {
        try {
            log.info("getting consultation messages on behalf of doctor");
            return ResponseEntity.ok().body(consultationService
                    .getAllMessagesGivenConsultationIdAndSessionId(consultationId, consultationSessionId,
                            pageNumber, elementsPerPage));
        } catch (ConsultationServiceException e) {
            return WooBoHttpError.of(e).toResponseEntity();
        }
    }

    @GetMapping("/patient/{userName}/history")
    @Operation(summary = "Get Patient's History, list of sessionId",
            description = "Retrieve all consultation sessions Ids .")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieves the Doctor's comments. ROL DOCTOR"),
            @ApiResponse(responseCode = "400", description = "Invalid status parameter"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<PatientConsultationSummary>>
    getPatientHistory(@PathVariable String userName,
                      @RequestParam int pageNumber, @RequestParam int elementsPerPage,
                     @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok().body(axSaludService.getUserHistory(userName, pageNumber, elementsPerPage));
        } catch (Exception e) {
            return WooBoHttpError.of(e).toResponseEntity();
        }
    }

    @GetMapping("/patient/{userName}/patientInformation")
    @Operation(summary = "Get patient information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient found"),
            @ApiResponse(responseCode = "301", description = "Forbidden"),
            @ApiResponse(responseCode = "410", description = "Password cannot be empty"),
            @ApiResponse(responseCode = "411", description = "User with username given already exists"),
            @ApiResponse(responseCode = "414", description = "Sponsor is not active or does not exist."),
            @ApiResponse(responseCode = "415", description = "User is not an active user")
    })
    public ResponseEntity<PatientViewDTO> getPatientData(@PathVariable String userName,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(axSaludService.get(userName));
        } catch (WooUserServiceException e) {
            log.error("Error while getting user data: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/patient/{userName}/docPrescriptions")
    @Operation(summary = "Get patient Prescriptions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient found"),
            @ApiResponse(responseCode = "301", description = "Forbidden")
    })
    public ResponseEntity<List<DoctorPrescriptionViewDTO>> getPatientPrescriptions(@PathVariable String userName,
                                                                             @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(axSaludService.getDoctorPrescriptions(userName));
        } catch (WooUserServiceException e) {
            log.error("Error while getting user data: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/patient/{userName}/labPrescription")
    @Operation(summary = "Get patient labPrescription")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient found"),
            @ApiResponse(responseCode = "301", description = "Forbidden")
    })
    public ResponseEntity<List<LabPrescriptionViewDTO>> getLabPrescriptions(@PathVariable String userName,
                                                                                   @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(axSaludService.getLabPrescriptions(userName));
        } catch (WooUserServiceException e) {
            log.error("Error while getting user data: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }


}
