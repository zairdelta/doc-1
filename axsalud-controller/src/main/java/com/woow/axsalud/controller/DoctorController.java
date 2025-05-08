package com.woow.axsalud.controller;

import com.woow.axsalud.data.consultation.ComentariosMedicos;
import com.woow.axsalud.data.consultation.ConsultationStatus;
import com.woow.axsalud.data.repository.ComentariosMedicosRepository;
import com.woow.axsalud.service.api.dto.ConsultationDTO;
import com.woow.axsalud.service.api.dto.DoctorCommentsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctor")
@Slf4j
public class DoctorController {

    private ComentariosMedicosRepository comentariosMedicosRepository;
    public DoctorController(final ComentariosMedicosRepository comentariosMedicosRepository) {
        this.comentariosMedicosRepository = comentariosMedicosRepository;
    }

    @GetMapping
    @Operation(summary = "Get Patient's comments",
            description = "Retrieve all Doctor's Comments given to a user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieves the Doctor's comments. ROL DOCTOR"),
            @ApiResponse(responseCode = "400", description = "Invalid status parameter"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<DoctorCommentsDTO>>
    getDoctorComment(@RequestParam ConsultationStatus status,
                     @AuthenticationPrincipal UserDetails userDetails) {
        List<ComentariosMedicos> comentariosMedicos =
                comentariosMedicosRepository.findByAxSaludWooUser_CoreUser_UserName(userDetails.getUsername());

        List<DoctorCommentsDTO> doctorCommentsDTOS =
                comentariosMedicos.stream()
                        .map(comentariosMedicos1 -> {
                            DoctorCommentsDTO doctorCommentsDTO = new DoctorCommentsDTO();
                            doctorCommentsDTO.setComment(comentariosMedicos1.getObservacionesMedicas());
                            return doctorCommentsDTO;
                        })
                        .collect(Collectors.toList());
        return ResponseEntity.ok().body(doctorCommentsDTOS);
    }
}
