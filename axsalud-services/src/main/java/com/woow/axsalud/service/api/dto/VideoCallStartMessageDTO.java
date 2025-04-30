package com.woow.axsalud.service.api.dto;

import lombok.Data;

@Data
public class VideoCallStartMessageDTO extends ConsultationMessageDTO {
    private VideoTokenDTO videoTokenDTO;
}
