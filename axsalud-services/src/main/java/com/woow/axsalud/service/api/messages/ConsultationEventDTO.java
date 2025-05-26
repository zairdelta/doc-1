package com.woow.axsalud.service.api.messages;

import com.woow.axsalud.service.api.dto.ConsultationMessgeTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ConsultationEventDTO<T> {
    private long id;
    private Map<String, Object> metadata;
    private LocalDateTime timeProcessed;
    private ConsultationMessgeTypeEnum messageType;
    private String version = "1.0.0";
    private T payload;
}
