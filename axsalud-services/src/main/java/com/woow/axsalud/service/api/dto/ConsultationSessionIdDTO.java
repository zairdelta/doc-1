package com.woow.axsalud.service.api.dto;

import com.woow.axsalud.data.consultation.ConsultationMessageEntity;
import com.woow.axsalud.data.consultation.ConsultationSessionStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private List<String> documentUrlList = new ArrayList<>();
}
