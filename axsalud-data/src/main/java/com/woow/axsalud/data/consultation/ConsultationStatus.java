package com.woow.axsalud.data.consultation;

public enum ConsultationStatus {
    WAITING_FOR_DOCTOR("WAITING_FOR_DOCTOR"),
    ON_GOING("ON_GOING"),
    SUSPENDED("SUSPENDED"),
    ABANDONED("ABANDONED"),
    FINISHED("FINISHED");

    private String status;

    ConsultationStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
