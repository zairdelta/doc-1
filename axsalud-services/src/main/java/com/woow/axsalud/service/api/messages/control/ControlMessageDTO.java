package com.woow.axsalud.service.api.messages.control;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ControlMessageDTO {
    private long id;
    private LocalDateTime timeProcessed;
    private ControlMessageType messageType;
    private String doctor;
    private String patient;
    private String sender;
}
