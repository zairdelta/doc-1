package com.woow.axsalud.service.api.messages.control;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionAbandonedDTO {
    private String userName;
    private String role;
    private String newConsultationSessionStatus;
    private String consultationId;
    private String consultationSessionId;
    private LocalDateTime lastTimeSeen;
}
