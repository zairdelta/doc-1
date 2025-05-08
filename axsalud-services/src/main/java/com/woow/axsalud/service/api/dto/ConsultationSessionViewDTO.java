package com.woow.axsalud.service.api.dto;

import com.woow.axsalud.data.consultation.ConsultationSessionStatus;
import com.woow.axsalud.data.consultation.DoctorPrescription;
import com.woow.axsalud.data.consultation.LaboratoryPrescription;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ConsultationSessionViewDTO {
    private String consultationId;
    private String consultationSessionId;
    private DoctorViewDTO doctorViewDTO;
    private PatientViewDTO patientViewDTO;
    private LocalDateTime startAt;
    private LocalDateTime finishedAt;
    private ConsultationSessionStatus status;
    private Set<DoctorPrescription> doctorPrescriptions;
    private Set<LaboratoryPrescription> laboratoryPrescriptions;
}
