package com.woow.axsalud.service.api.dto;

public enum ConsultationMessgeTypeEnum {
    ERROR("ERROR"),
    WELCOME("WELCOME"),
    TEXT_MESSAGE("TEXT_MESSAGE"),
    START_VIDEO_CALL("START_VIDEO_CALL");

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
