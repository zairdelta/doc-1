package com.woow.axsalud.common.multitenant;

import java.util.UUID;

public class IdGenerator {
    private static final String FAMILY_CARD_ID = "FCX1";
    public static String getHID(String tenantId, int userId) {

        String userIdString = String.format("%03d", userId);
        String randomUUID = UUID.randomUUID().toString().substring(0, 2);
        String generatedId = tenantId + "-" + userIdString + randomUUID;
        return generatedId;

    }

    public static String getGenericFamilyCardHID(int userId) {
        String userIdString;
        if (userId < 1000) {
            userIdString = String.format("%08d", userId);
        } else {
            userIdString = Integer.toString(userId);
        }
        String randomUUID = UUID.randomUUID().toString().substring(0, 2);
        String generatedId = FAMILY_CARD_ID + "-" + userIdString + randomUUID;
        return generatedId;
    }

    public static String getRandomUserName() {
        String randomUUID1 = UUID.randomUUID().toString().substring(0, 5);
        String randomUUID2 = UUID.randomUUID().toString().substring(0, 3);
        String randomUUID3 = UUID.randomUUID().toString().substring(0, 3);
        return randomUUID1 + randomUUID2 + randomUUID3;
    }

    public static String getRandomPassword() {
        String randomUUID1 = UUID.randomUUID().toString().substring(0, 5);
        String randomUUID2 = UUID.randomUUID().toString().substring(0, 3);
        String randomUUID3 = UUID.randomUUID().toString().substring(0, 3);
        return (randomUUID1 + randomUUID2 + randomUUID3).substring(0, 3);
    }

    public static String getTenantId(String shortName) {
        String randomUUID = UUID.randomUUID().toString().substring(0, 1);
        String tenantId = shortName
            .replaceAll("\\s", "") + randomUUID;
        return tenantId;
    }

}
