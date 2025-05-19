package com.woow.axsalud.service.api.messages;

import com.woow.axsalud.service.api.dto.ConsultationMessgeTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsultationEventDTO {
    private long id;
    private LocalDateTime timeProcessed;
    private ConsultationMessgeTypeEnum messageType;
    private String version;
}
