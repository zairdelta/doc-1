package com.woow.axsalud.data.consultation;

public enum ConsultationSessionStatus {

    WAITING_FOR_DOCTOR("WAITING_FOR_DOCTOR"),
    CONFIRMING_PARTIES("CONFIRMING_PARTIES"),
    CONNECTED("CONNECTED"),

    CONNECTING("CONNECTING"),
    ABANDONED("ABANDONED"),
    ON_GOING("ON_GOING"),
    SUSPENDED("SUSPENDED"),
    FINISHED("FINISHED");

    private String status;

    ConsultationSessionStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
