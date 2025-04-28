package com.woow.axsalud.controller;

import com.woow.axsalud.controller.exception.WooBoHttpError;
import com.woow.axsalud.data.consultation.ConsultationStatus;
import com.woow.axsalud.service.api.ConsultationService;
import com.woow.axsalud.service.api.dto.ConsultationDTO;
import com.woow.axsalud.service.api.dto.SymptomsDTO;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                                               @AuthenticationPrincipal UserDetails userDetails,
                                               @RequestParam(name = "pageNumber") int pageNumber,
                                               @RequestParam(name = "totalByPage") int totalByPage) {
        return ResponseEntity.ok().build();
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

    @PostMapping("{consultationId}/file")
    public ResponseEntity<Map> upload(@PathVariable String consultationId,
                                         @AuthenticationPrincipal UserDetails userDetails,
                                    @RequestBody MultipartFile file) {
        try {
            Map<String, Long> docIdMap = new HashMap<>();
            Long docId = consultationService.appendDocument(userDetails.getUsername(),
                    consultationId, file);
            docIdMap.put("fileId", docId);
            return ResponseEntity
                    .ok(docIdMap);
        } catch (ConsultationServiceException e) {
            return WooBoHttpError.of(e).toResponseEntity();
        }

    }


    @GetMapping("/{consultationId}/file/{fileId}")
    public ResponseEntity<String> downloadFile(@PathVariable String consultationId,
                                               @PathVariable long fileId,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String signedUrl = consultationService.downloadDocument(userDetails.getUsername(), consultationId, fileId);
            return ResponseEntity.ok(signedUrl);
        } catch (ConsultationServiceException e) {
            return WooBoHttpError.of(e).toResponseEntity();
        }
    }

}