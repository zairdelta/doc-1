package com.woow.axsalud.service.api.dto;

public enum ConsultationMessgeTypeEnum {
    ERROR("ERROR"),
    WELCOME("WELCOME"),
    TEXT_MESSAGE("TEXT_MESSAGE"),
    START_VIDEO_CALL("START_VIDEO_CALL"),
    SESSION_ESTABLISHED("SESSION_ESTABLISHED"),
    SESSION_END("SESSION_END"),
    NEW_CONSULTATION_CREATED("NEW_CONSULTATION_CREATED"),
    CONSULTATION_ASSIGNED("CONSULTATION_ASSIGNED"),
    FILE_UPLOADED("FILE_UPLOADED");

    private String type;
    ConsultationMessgeTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static ConsultationMessgeTypeEnum fromString(String type) {
        for (ConsultationMessgeTypeEnum value : ConsultationMessgeTypeEnum.values()) {
            if (value.getType().equalsIgnoreCase(type)) {
                return value;
            }
        }
        return ConsultationMessgeTypeEnum.TEXT_MESSAGE; // default fallback
    }


}
