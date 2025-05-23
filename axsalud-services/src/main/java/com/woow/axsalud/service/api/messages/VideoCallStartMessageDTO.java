package com.woow.axsalud.service.api.messages;

import com.woow.axsalud.service.api.dto.VideoTokenDTO;
import lombok.Data;

@Data
public class VideoCallStartMessageDTO extends ConsultationMessageDTO {
    private VideoTokenDTO videoTokenDTO;
}
