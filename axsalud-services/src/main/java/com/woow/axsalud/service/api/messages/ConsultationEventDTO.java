package com.woow.axsalud.service.api.messages;

import com.woow.axsalud.service.api.dto.ConsultationMessgeTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ConsultationEventDTO<T> {
    private long id;
    private LocalDateTime timeProcessed;
    private ConsultationMessgeTypeEnum messageType;
    private String version;
    private T payload;
    private Map<String, Object> metadata;
}
