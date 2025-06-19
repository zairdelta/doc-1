package com.woow.axsalud.service.api.messages;

import com.woow.axsalud.data.consultation.ConsultationMessageEntity;
import com.woow.axsalud.service.api.dto.ConsultationMessgeTypeEnum;
import com.woow.axsalud.service.api.dto.FileResponseDTO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsultationMessageDTO {
    private String sender;
    private String receiver;
    private String content;
    private String consultationId;
    private String consultationSessionId;

    public static ConsultationEventDTO from(ConsultationMessageEntity consultationMessageEntity) {

        ConsultationEventDTO<ConsultationMessageDTO> consultationEventDTO = new ConsultationEventDTO<>();

        ConsultationMessageDTO consultationMessageDTO = new ConsultationMessageDTO();
        consultationEventDTO.setId(consultationMessageEntity.getId());
        consultationMessageDTO
                .setConsultationSessionId(consultationMessageEntity
                        .getConsultationSession().getConsultationSessionId().toString());
        consultationMessageDTO.setConsultationId(consultationMessageEntity
                .getConsultationSession().getConsultation().getConsultationId().toString());
        consultationEventDTO.setTimeProcessed(consultationMessageEntity.getTimestamp());
        consultationEventDTO.setMessageType(
                ConsultationMessgeTypeEnum.fromString(consultationMessageEntity.getMessageType()));
        consultationMessageDTO.setContent(consultationMessageEntity.getContent());
        consultationMessageDTO.setSender(consultationMessageEntity.getSentBy().getCoreUser().getUserName());


        consultationEventDTO.setPayload(consultationMessageDTO);
        return consultationEventDTO;
    }
    public static ConsultationEventDTO from(FileResponseDTO fileResponseDTO, String sender) {

        ConsultationEventDTO<ConsultationMessageDTO> consultationEventDTO = new ConsultationEventDTO<>();

        ConsultationMessageDTO consultationMessageDTO = new ConsultationMessageDTO();
        consultationEventDTO.setId(0);
        consultationMessageDTO
                .setConsultationSessionId(fileResponseDTO.getConsultationSessionId());
        consultationMessageDTO.setConsultationId(fileResponseDTO.getConsultationId());
        consultationEventDTO.setTimeProcessed(LocalDateTime.now());
        consultationEventDTO.setMessageType(ConsultationMessgeTypeEnum.FILE_UPLOADED);
        consultationMessageDTO.setContent(String.valueOf(fileResponseDTO.getId()));
        consultationMessageDTO.setSender(sender);
        consultationEventDTO.setPayload(consultationMessageDTO);
        return consultationEventDTO;
    }

}
