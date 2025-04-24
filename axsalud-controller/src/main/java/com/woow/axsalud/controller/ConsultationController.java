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

@RestController
@RequestMapping("/api/consultation")
@Slf4j
public class ConsultationController {

    private ConsultationService consultationService;
    private final SimpMessagingTemplate messagingTemplate;

    public ConsultationController(ConsultationService consultationService,
                                  SimpMessagingTemplate messagingTemplate) {
        this.consultationService = consultationService;
        this.messagingTemplate = messagingTemplate;
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
            ConsultationMessage welcomeMessage = new ConsultationMessage();
            welcomeMessage.setSender(userName);
            welcomeMessage.setReceiver(consultationDTO.getPatient());
            welcomeMessage.setConsultationId(consultationId);
            welcomeMessage.setContent("👋 " + consultationDTO.getWelcomeMessage());
            welcomeMessage.setMessageType("WELCOME");
            consultationService.addMessage(welcomeMessage);

            messagingTemplate.convertAndSendToUser(
                    consultationDTO.getPatient(),
                    "/queue/messages",
                    welcomeMessage
            );

            return ResponseEntity.ok(consultationDTO);

        } catch (ConsultationServiceException e) {
            return WooBoHttpError.of(e).toResponseEntity();
        }
    }


}