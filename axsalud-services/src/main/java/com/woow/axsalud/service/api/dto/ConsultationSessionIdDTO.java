package com.woow.axsalud.service.api.dto;

import com.woow.axsalud.data.consultation.ConsultationSession;
import com.woow.axsalud.data.consultation.ConsultationSessionStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
public class ConsultationSessionIdDTO {

    private long id;
    private String consultationSessionId;
    private DoctorViewDTO doctorViewDTO;
    private String diagnosis;
    private LocalDateTime startAt;
    private LocalDateTime finishedAt;
    private ConsultationSessionStatus status;
    private List<ConsultationMessageDTO> messages = new ArrayList<>();
    private List<ConsultationDocumentDTO> consultationDocumentDTOS = new ArrayList<>();

    public static ConsultationSessionIdDTO from(final ConsultationSession consultationSession) {
        ConsultationSessionIdDTO consultationSessionIdDTO = new ConsultationSessionIdDTO();
        consultationSessionIdDTO.setConsultationSessionId(consultationSessionIdDTO.consultationSessionId);
        consultationSessionIdDTO.setStartAt(consultationSession.getStartAt());
        consultationSessionIdDTO.setId(consultationSessionIdDTO.getId());
        consultationSessionIdDTO.setDiagnosis(consultationSessionIdDTO.getDiagnosis());
        consultationSessionIdDTO.setFinishedAt(consultationSession.getFinishedAt());
        consultationSessionIdDTO.setStatus(consultationSession.getStatus());

        consultationSessionIdDTO.setMessages(consultationSession.getMessages()
                .stream()
                .filter(Objects::nonNull)
                .map(ConsultationMessageDTO::from)
                .collect(Collectors.toList()));

        consultationSessionIdDTO.setConsultationDocumentDTOS(consultationSession.getDocuments()
                .stream()
                .filter(Objects::nonNull)
                .map(ConsultationDocumentDTO::from)
                .collect(Collectors.toList()));


        return consultationSessionIdDTO;

    }
}
