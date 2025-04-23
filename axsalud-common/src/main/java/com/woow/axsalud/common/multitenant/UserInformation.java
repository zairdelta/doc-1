package com.woow.axsalud.common.multitenant;

import lombok.Data;

@Data
public class UserInformation {
    private String userName;
    private int userId;
    private String sponsor;
}
