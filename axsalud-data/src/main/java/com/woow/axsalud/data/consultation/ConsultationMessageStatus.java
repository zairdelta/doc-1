package com.woow.axsalud.data.consultation;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public enum ConsultationMessageStatus {
    DELIVERED("DELIVERED"),
    SERVER_RECEIVED("SERVER_RECEIVED"),
    SENT_TO_RECEIVER("SENT_TO_RECEIVER");

    private String status;

    ConsultationMessageStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

}
