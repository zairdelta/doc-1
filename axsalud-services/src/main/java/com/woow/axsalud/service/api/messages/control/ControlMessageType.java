package com.woow.axsalud.service.api.messages.control;

public enum ControlMessageType {

    DOCTOR_ASSIGNED("DOCTOR_ASSIGNED"),
    PARTY_READY("PARTY_READY"),
    CHAT_READY("CHAT_READY"),
    PING("PING"),
    PONG("PONG");

    private String type;
    ControlMessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
