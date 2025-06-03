package com.woow.axsalud.data.repository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface PatientConsultationSummary {
    Long getId();
    UUID getConsultationId();
    UUID getConsultationSessionId();
    String getDoctorName();
    String getSymptoms();
    String getStatus();
    LocalDateTime getCreatedAt();
}
