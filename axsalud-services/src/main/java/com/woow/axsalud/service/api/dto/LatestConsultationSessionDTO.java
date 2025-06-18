package com.woow.axsalud.service.api.dto;

import com.woow.axsalud.data.consultation.ConsultationSession;
import com.woow.axsalud.data.consultation.ConsultationSessionStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LatestConsultationSessionDTO {
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;
    private ConsultationSessionStatus status;
    private String consultationSessionId;

    public static LatestConsultationSessionDTO from(ConsultationSession consultationSession) {

        LatestConsultationSessionDTO latestConsultationSessionDTO = new LatestConsultationSessionDTO();
        if(consultationSession != null) {
            latestConsultationSessionDTO.setConsultationSessionId(consultationSession.getConsultationSessionId().toString());
            latestConsultationSessionDTO.setStatus(consultationSession.getStatus());
            latestConsultationSessionDTO.setCreatedAt(consultationSession.getCreatedAt());
            latestConsultationSessionDTO.setFinishedAt(consultationSession.getFinishedAt());
        }
        return latestConsultationSessionDTO;
    }
}
