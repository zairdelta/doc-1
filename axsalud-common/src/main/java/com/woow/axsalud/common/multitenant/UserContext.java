package com.woow.axsalud.common.multitenant;

public class UserContext {
    private static final ThreadLocal<UserInformation> CURRENT_USER = new ThreadLocal<>();

    public static UserInformation getUserInformation() {
        return CURRENT_USER.get();
    }

    public static void setCurrentUserInformation(UserInformation userInformation) {
        CURRENT_USER.set(userInformation);
    }
}
