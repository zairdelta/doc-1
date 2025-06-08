package com.woow.axsalud.common;

public enum UserStatesEnum {

    ONLINE("ONLINE"),
    OFFLINE("OFFLINE"),
    WAITING_FROM_DOCTOR_ABANDONED("WAITING_FROM_DOCTOR_ABANDONED"),
    UNAVAILABLE("UNAVAILABLE");

    String state;

    UserStatesEnum(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}
