package com.woow.axsalud.controller;

import com.woow.axsalud.service.api.ConsultationService;
import com.woow.axsalud.service.api.dto.ConsultationMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
public class ConsultationWebSocket {

    private ConsultationService consultationService;


    public ConsultationWebSocket(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @MessageMapping("/consultation/{consultationId}/session/{consultationSessionId}/private")
    public void sendPrivateMessage(
            @DestinationVariable String consultationId,
            @DestinationVariable String consultationSessionId,
            @Payload ConsultationMessageDTO consultationMessage,
            Principal principal
    ) {
        consultationMessage.setConsultationId(consultationId);
        consultationMessage.setConsultationSessionId(consultationSessionId);
        consultationMessage.setSender(principal.getName());
        log.info("consultationMessage Received: {}", consultationMessage);
        consultationService.handledConsultationMessage(consultationMessage);
    }
}
