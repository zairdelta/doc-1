package com.woow.axsalud.controller;

import com.woow.axsalud.service.api.ConsultationService;
import com.woow.axsalud.service.api.dto.ConsultationMessage;
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

    @MessageMapping("/consultation/{consultationId}/private")
    public void sendPrivateMessage(@DestinationVariable String consultationId,
                                   @Payload ConsultationMessage consultationMessage,
                                   Principal principal) {
        consultationMessage.setConsultationId(consultationId);
        consultationMessage.setSender(principal.getName());
        consultationService.handledConsultationMessage(consultationMessage);
    }
}
