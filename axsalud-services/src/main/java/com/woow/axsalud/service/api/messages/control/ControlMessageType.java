package com.woow.axsalud.service.api.messages.control;

public enum ControlMessageType {

    DOCTOR_ASSIGNED("DOCTOR_ASSIGNED"),
    PARTY_READY("PARTY_READY"),
    CHAT_READY("CHAT_READY"),
    PARTY_CONNECTED("PARTY_CONNECTED"),
    CONNECTED("CONNECTED"),
    SESSION_ABANDONED_BY_PARTY("SESSION_ABANDONED_BY_PARTY"),
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
