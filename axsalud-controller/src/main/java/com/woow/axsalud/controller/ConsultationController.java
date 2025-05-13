package com.woow.axsalud.controller;

import com.woow.axsalud.controller.exception.WooBoHttpError;
import com.woow.axsalud.data.consultation.ConsultationStatus;
import com.woow.axsalud.data.consultation.LaboratoryPrescription;
import com.woow.axsalud.service.api.ConsultationService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/consultation")
@Slf4j
public class ConsultationController {

    private ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @GetMapping
    @Operation(summary = "Get Consultations by Status",
            description = "Retrieve all consultations filtered by their current status, ordered by creation time ascending. "
                    + "You must pass the status as a query parameter. Allowed statuses: WAITING_FOR_DOCTOR, ON_GOING, SUSPENDED, FINISHED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved consultations list"),
            @ApiResponse(responseCode = "400", description = "Invalid status parameter"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<ConsultationDTO>> getAllByStatus(@RequestParam ConsultationStatus status) {
        return ResponseEntity.ok().body(consultationService.getConsultationsByStatus(status));
    }

    @GetMapping("/{consultationId}")
    public ResponseEntity<ConsultationDTO> get(@PathVariable String consultationId,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok().body(consultationService
                .getbyConsultationId(userDetails.getUsername(),
                        consultationId));
    }


    @PostMapping
    public ResponseEntity<ConsultationDTO> create(@RequestBody final SymptomsDTO symptomsDTO,
                                                  @AuthenticationPrincipal UserDetails userDetails) {

        try {
            return ResponseEntity
                    .ok(consultationService.create(symptomsDTO, userDetails.getUsername()));
        } catch (WooUserServiceException e) {
            return WooBoHttpError.of(e).toResponseEntity();
        }
    }

    @PutMapping("/{consultationId}/sessionId/{consultationSessionId}/doctor")
    public ResponseEntity<ConsultationDTO>
    assignDoctor(@PathVariable String consultationId,
                 @PathVariable String consultationSessionId,
                 @AuthenticationPrincipal UserDetails userDetails) {
        try {

            String userName = userDetails.getUsername();
            ConsultationDTO consultationDTO =
                    consultationService.assign(userName, consultationId, consultationSessionId);

            return ResponseEntity.ok(consultationDTO);

        } catch (ConsultationServiceException e) {
            return WooBoHttpError.of(e).toResponseEntity();
        }
    }

    @PostMapping("{consultationId}/sessionId/{consultationSessionId}/file")
    public ResponseEntity<FileResponseDTO> upload(@PathVariable String consultationId,
                                                  @PathVariable String consultationSessionId,
                                                  @AuthenticationPrincipal UserDetails userDetails,
                                                  @RequestBody MultipartFile file) {
        try {

            return ResponseEntity.ok().body(consultationService.appendDocument(userDetails.getUsername(),
                    consultationSessionId, file));
        } catch (ConsultationServiceException e) {
            return WooBoHttpError.of(e).toResponseEntity();
        }

    }


    @GetMapping("{consultationId}/sessionId/{consultationSessionId}/file/{fileId}")
    public ResponseEntity<FileResponseDTO> downloadFile(@PathVariable String consultationId,
                                               @PathVariable long fileId,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok().body(consultationService
                    .downloadDocument(userDetails.getUsername(), consultationId, fileId));
        } catch (ConsultationServiceException e) {
            return WooBoHttpError.of(e).toResponseEntity();
        }
    }

    @GetMapping("/consultationMessages")
    public ResponseEntity<ConsultationMessagesPagingDTO> getConsultationAllUserConsultationMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam int pageNumber, @RequestParam int elementsPerPage) {
        try {
            return ResponseEntity.ok().body(consultationService
                    .getAllMessageByUserNameUsingPaginationPagination(userDetails.getUsername(),
                            pageNumber, elementsPerPage));
        } catch (ConsultationServiceException e) {
            return WooBoHttpError.of(e).toResponseEntity();
        }
    }


    @GetMapping("{consultationId}/sessionId/{consultationSessionId}/messages")
    public ResponseEntity<ConsultationMessagesPagingDTO> getConsultationMessagesGivenConsultationId
            (@PathVariable String consultationId,
             @PathVariable String consultationSessionId,
             @RequestParam int pageNumber,
             @RequestParam int elementsPerPage,
             @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok().body(consultationService.
                    getAllMessagesGivenConsultationIdAndSessionId(consultationId, consultationSessionId,
                            pageNumber, elementsPerPage));
        } catch (ConsultationServiceException e) {
            return WooBoHttpError.of(e).toResponseEntity();
        }
    }

    @GetMapping("{consultationId}/sessionId/{consultationSessionId}/view")
    public ResponseEntity<ConsultationSessionViewDTO> getConsultationSessionView
            (@PathVariable String consultationId,
             @PathVariable String consultationSessionId,
             @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok().body(consultationService.
                    getConsultationSession(userDetails.getUsername(), consultationSessionId));
        } catch (ConsultationServiceException e) {
            return WooBoHttpError.of(e).toResponseEntity();
        }
    }

    @PutMapping("{consultationId}/sessionId/{consultationSessionId}/doctorPrescription")
    public ResponseEntity<ConsultationSessionViewDTO> addDoctorPrescription
            (@PathVariable String consultationId,
             @PathVariable String consultationSessionId,
             @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody List<DoctorPrescriptionDTO> doctorPrescriptionsDTO) {
        try {
            consultationService
                    .addDoctorPrescriptions(userDetails.getUsername(),
                            consultationId,
                            consultationSessionId,
                            doctorPrescriptionsDTO);
            return ResponseEntity.ok().build();
        }  catch (ConsultationServiceException e) {
            return WooBoHttpError.of(e).toResponseEntity();
        } catch (Exception e) {
            return WooBoHttpError.of(e).toResponseEntity();
        }
    }

    @PutMapping("{consultationId}/sessionId/{consultationSessionId}/laboratoryPrescriptions")
    public ResponseEntity<ConsultationSessionViewDTO> addLaboratoryPrescriptions
            (@PathVariable String consultationId,
             @PathVariable String consultationSessionId,
             @AuthenticationPrincipal UserDetails userDetails,
             @RequestBody List<LaboratoryPrescriptionDTO> laboratoryPrescriptions) {
        try {
            consultationService
                    .addLaboratoryPrescriptions(userDetails.getUsername(),
                            consultationId, consultationSessionId,
                            laboratoryPrescriptions);
            return ResponseEntity.ok().build();
        } catch (ConsultationServiceException e) {
            return WooBoHttpError.of(e).toResponseEntity();
        } catch (Exception e) {
            return WooBoHttpError.of(e).toResponseEntity();
        }
    }



}