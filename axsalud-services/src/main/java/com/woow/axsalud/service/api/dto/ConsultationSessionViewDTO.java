package com.woow.axsalud.service.api.dto;

import com.woow.axsalud.data.consultation.ConsultationSessionStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsultationSessionViewDTO {
    private String consultationId;
    private String consultationSessionId;
    private DoctorViewDTO doctorViewDTO;
    private PatientViewDTO patientViewDTO;
    private LocalDateTime startAt;
    private LocalDateTime finishedAt;
    private ConsultationSessionStatus status;

}
