package com.woow.axsalud.controller;

import com.woow.axsalud.controller.exception.WooBoHttpError;
import com.woow.axsalud.service.api.ConsultationService;
import com.woow.axsalud.service.api.dto.ConsultationMessage;
import com.woow.axsalud.service.api.dto.ConsultationDTO;
import com.woow.axsalud.service.api.dto.SymptomsDTO;
import com.woow.axsalud.service.api.exception.ConsultationServiceException;
import com.woow.core.service.api.exception.WooUserServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/consultation")
@Slf4j
public class ConsultationController {

    private ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @GetMapping("/{consultationId}")
    public ResponseEntity<ConsultationDTO> get(@PathVariable String consultationId,
                                               @AuthenticationPrincipal UserDetails userDetails) {
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

    @PutMapping("/{consultationId}/doctor")
    public ResponseEntity<ConsultationDTO>
    assignDoctor(@PathVariable String consultationId,
                 @AuthenticationPrincipal UserDetails userDetails) {
        try {

            String userName = userDetails.getUsername();
            ConsultationDTO consultationDTO =
                    consultationService.assign(userName, consultationId);

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