package com.woow.axsalud.service.api.dto;

import lombok.Data;

@Data
public class SessionCreatedNotificationDTO {
    private String consultationId;
    private String consultationSessionId;
}
