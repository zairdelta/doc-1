package com.woow.axsalud.data.consultation;

public enum PartyConsultationStatus {

    READY("READY"),
    OFFLINE("OFFLINE"),
    ONLINE("ONLINE");

    private String status;

    PartyConsultationStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
