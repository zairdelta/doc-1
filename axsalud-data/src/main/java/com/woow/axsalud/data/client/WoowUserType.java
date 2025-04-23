package com.woow.axsalud.data.client;

public enum WoowUserType {
    DOCTOR("DOCTOR"),
    HEALTH_SERVICE_PROVIDER("HEALTH_SERVICE_PROVIDER"),
    PATIENT("PATIENT"),
    PSYCHOLOGIST("PSYCHOLOGIST");

    private String userType;
    WoowUserType(String userType) {
        this.userType = userType;
    }

    public String getUserType() {
        return this.userType;
    }
}
