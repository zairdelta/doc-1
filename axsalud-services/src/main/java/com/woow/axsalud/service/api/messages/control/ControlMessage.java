package com.woow.axsalud.service.api.messages.control;

import lombok.Data;

import java.util.List;

@Data
public class ControlMessage {
    private ControlMessageDTO controlMessageDTO;
    private String consultationId;
    private String consultationSessionId;
    private String userName;
    private List<String> roles;
}
