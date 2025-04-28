package com.woow.axsalud.service.api.dto;

import com.woow.axsalud.data.consultation.Consultation;
import com.woow.axsalud.data.consultation.ConsultationSession;
import com.woow.axsalud.data.consultation.ConsultationStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private List<ConsultationSessionIdDTO> consultationSessionIdDTOList =
            new ArrayList<>();

    public static ConsultationDTO from(Consultation consultation) {
        ConsultationDTO consultationDTO = new ConsultationDTO();

        consultationDTO.setConsultationId(consultation.getConsultationId().toString());
        consultationDTO.setId(consultation.getId());
        consultationDTO.setStatus(consultation.getStatus());
        consultationDTO.setSymptoms(consultationDTO.getSymptoms());
        consultationDTO.setPatient(consultationDTO.getPatient());
        consultationDTO.setCurrentSessionIdIfExists(consultationDTO.getCurrentSessionIdIfExists());
        consultationDTO.setFinishedAt(consultationDTO.getFinishedAt());
        consultationDTO.setCreatedAt(consultation.getCreatedAt());

        consultationDTO
                .setConsultationSessionIdDTOList(consultation.getSessions()
                .stream()
                        .filter(Objects::nonNull)
                .map(ConsultationSessionIdDTO::from)
                .collect(Collectors.toList()));

        return consultationDTO;
    }


}
