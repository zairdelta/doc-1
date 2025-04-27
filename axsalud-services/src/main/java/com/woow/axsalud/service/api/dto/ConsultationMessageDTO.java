package com.woow.axsalud.service.api.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ConsultationMessageDTO {
    private String sender;
    private String receiver;
    private String content;
    private String consultationId;
    private String consultationSessionId;
    private LocalDate startAt;
    private LocalDate finishedAt;
    private ChatStatus status = ChatStatus.STARTED;
    private String messageType;
}
