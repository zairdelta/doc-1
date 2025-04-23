package com.woow.axsalud.service.api.dto;

public enum ChatStatus {

    STARTED("STARTED"),
    ACTIVE("ACTIVE"),
    SUSPENDED("SUSPENDED"),
    FINISHED("FINISHED");

    String status;
    ChatStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
