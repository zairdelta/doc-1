package com.woow.axsalud.common;

public enum UserStatesEnum {

    ONLINE("ONLINE"),
    OFFLINE("OFFLINE"),
    UNAVAILABLE("UNAVAILABLE");

    String state;

    UserStatesEnum(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}
