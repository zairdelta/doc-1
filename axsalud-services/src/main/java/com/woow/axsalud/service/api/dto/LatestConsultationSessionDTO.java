package com.woow.axsalud.service.api.dto;

import com.woow.axsalud.data.consultation.ConsultationSession;
import com.woow.axsalud.data.consultation.ConsultationSessionStatus;
import com.woow.axsalud.data.consultation.PartyConsultationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LatestConsultationSessionDTO {
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;
    private ConsultationSessionStatus status;
    private String consultationSessionId;
    private String consultationId;

    public static LatestConsultationSessionDTO from(ConsultationSession consultationSession) {

        LatestConsultationSessionDTO latestConsultationSessionDTO = new LatestConsultationSessionDTO();
        if(consultationSession != null) {
            latestConsultationSessionDTO.setConsultationSessionId(consultationSession.getConsultationSessionId().toString());

            if(   consultationSession.getPatientStatus() == PartyConsultationStatus.DROPPED) {
                latestConsultationSessionDTO.setStatus(ConsultationSessionStatus.ABANDONED);
            } else {
                latestConsultationSessionDTO.setStatus(consultationSession.getStatus());
            }

            latestConsultationSessionDTO.setCreatedAt(consultationSession.getCreatedAt());
            latestConsultationSessionDTO.setFinishedAt(consultationSession.getFinishedAt());
            latestConsultationSessionDTO.setConsultationId(consultationSession.getConsultation().getConsultationId().toString());
        }
        return latestConsultationSessionDTO;
    }
}
