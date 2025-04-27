package com.woow.axsalud.service.api.dto;

import com.woow.axsalud.data.consultation.ConsultationStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ConsultationDTO {
    private long id;
    private String consultationId;
    private String currentSessionIdIfExists;
    private String patient;
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;
    private String symptoms = "";
    private ConsultationStatus status;
    private String welcomeMessage;
    private List<ConsultationSessionIdDTO> consultationSessionIdDTOList = new ArrayList<>();
}
