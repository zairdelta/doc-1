package com.woow.axsalud.service.api.messages;

import com.woow.axsalud.data.consultation.ConsultationMessageEntity;
import com.woow.axsalud.service.api.dto.ConsultationMessgeTypeEnum;
import com.woow.axsalud.service.api.dto.FileResponseDTO;
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
    private ConsultationMessgeTypeEnum messageType;

    public static ConsultationMessageDTO from(ConsultationMessageEntity consultationMessageEntity) {
        ConsultationMessageDTO consultationMessageDTO = new ConsultationMessageDTO();
        consultationMessageDTO.setId(consultationMessageEntity.getId());
        consultationMessageDTO
                .setConsultationSessionId(consultationMessageEntity
                        .getConsultationSession().getConsultationSessionId().toString());
        consultationMessageDTO.setConsultationId(consultationMessageEntity
                .getConsultationSession().getConsultation().getConsultationId().toString());
        consultationMessageDTO.setTimeProcessed(consultationMessageEntity.getTimestamp());
        consultationMessageDTO.setMessageType(
                ConsultationMessgeTypeEnum.fromString(consultationMessageEntity.getMessageType()));
        consultationMessageDTO.setContent(consultationMessageEntity.getContent());
        consultationMessageDTO.setSender(consultationMessageEntity.getSentBy().getCoreUser().getUserName());
        return consultationMessageDTO;
    }
    public static ConsultationMessageDTO from(FileResponseDTO fileResponseDTO, String sender) {
        ConsultationMessageDTO consultationMessageDTO = new ConsultationMessageDTO();
        consultationMessageDTO.setId(0);
        consultationMessageDTO
                .setConsultationSessionId(fileResponseDTO.getConsultationSessionId());
        consultationMessageDTO.setConsultationId(fileResponseDTO.getConsultationId());
        consultationMessageDTO.setTimeProcessed(LocalDateTime.now());
        consultationMessageDTO.setMessageType(ConsultationMessgeTypeEnum.FILE_UPLOADED);
        consultationMessageDTO.setContent(String.valueOf(fileResponseDTO.getId()));
        consultationMessageDTO.setSender(sender);
        return consultationMessageDTO;
    }

}
