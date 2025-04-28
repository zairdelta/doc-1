package com.woow.axsalud.service.api.dto;

import com.woow.axsalud.data.consultation.ConsultationMessageEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsultationMessageDTO {
    private long id;
    private String sender;
    private String receiver;
    private String content;
    private String consultationId;
    private String consultationSessionId;
    private LocalDateTime timeProcessed;
    private ChatStatus status = ChatStatus.STARTED;
    private String messageType;

    public static ConsultationMessageDTO from(ConsultationMessageEntity consultationMessageEntity) {
        ConsultationMessageDTO consultationMessageDTO = new ConsultationMessageDTO();
        consultationMessageDTO.setId(consultationMessageEntity.getId());
        consultationMessageDTO
                .setConsultationSessionId(consultationMessageEntity
                        .getConsultationSession().getConsultationSessionId().toString());
        consultationMessageDTO.setConsultationId(consultationMessageEntity
                .getConsultationSession().getConsultation().getConsultationId().toString());
        consultationMessageDTO.setTimeProcessed(consultationMessageEntity.getTimestamp());
        consultationMessageDTO.setMessageType(consultationMessageEntity.getMessageType());
        consultationMessageDTO.setContent(consultationMessageEntity.getContent());
        consultationMessageDTO.setSender(consultationMessageEntity.getSentBy().getCoreUser().getUserName());
        return consultationMessageDTO;
    }
}
