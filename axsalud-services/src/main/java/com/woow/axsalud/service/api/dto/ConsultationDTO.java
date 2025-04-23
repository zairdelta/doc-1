package com.woow.axsalud.service.api.dto;

import com.woow.axsalud.data.client.Symptoms;
import com.woow.axsalud.data.consultation.ConsultationStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class ConsultationDTO {
    private long id;
    private String consultationId;
    private String patient;
    private String doctor;
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;
    private List<SymptomsDTO> symptoms = new ArrayList<>();
    private ConsultationStatus status;
    private String welcomeMessage;
}
