package com.woow.axsalud.service.api.dto;

import com.woow.axsalud.data.consultation.ConsultationSession;
import com.woow.axsalud.data.consultation.ConsultationSessionStatus;
import com.woow.axsalud.service.api.messages.ConsultationEventDTO;
import com.woow.axsalud.service.api.messages.ConsultationMessageDTO;
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
    private List<ConsultationEventDTO> messages = new ArrayList<>();
    private List<ConsultationDocumentDTO> consultationDocumentDTOS = new ArrayList<>();
    private List<LaboratoryPrescriptionDTO> laboratoryPrescriptionDTO = new ArrayList<>();
    private List<DoctorPrescriptionDTO> doctorPrescriptionDTO = new ArrayList<>();

    public static ConsultationSessionIdDTO from(final ConsultationSession consultationSession) {
        ConsultationSessionIdDTO consultationSessionIdDTO = new ConsultationSessionIdDTO();
        consultationSessionIdDTO.setConsultationSessionId(consultationSession.getConsultationSessionId().toString());
        consultationSessionIdDTO.setStartAt(consultationSession.getStartAt());
        consultationSessionIdDTO.setId(consultationSession.getId());
        consultationSessionIdDTO.setFinishedAt(consultationSession.getFinishedAt());
        consultationSessionIdDTO.setStatus(consultationSession.getStatus());

        /* message will need to be called in a different request as it is a heavy query
        consultationSessionIdDTO.setMessages(consultationSession.getMessages()
                .stream()
                .filter(Objects::nonNull)
                .map(ConsultationMessageDTO::from)
                .collect(Collectors.toList()));
        */

        consultationSessionIdDTO.setConsultationDocumentDTOS(consultationSession.getDocuments()
                .stream()
                .filter(Objects::nonNull)
                .map(ConsultationDocumentDTO::from)
                .collect(Collectors.toList()));


        return consultationSessionIdDTO;

    }
}
