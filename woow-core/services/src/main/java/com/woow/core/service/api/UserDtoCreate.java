package com.woow.core.service.api;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class UserDtoCreate {

    private Date birth;
    private String name;
    private String userName;
    private String lastName;
    private String country;
    private String password;
    private String email;
    private String serviceProvider;
    private String acceptTermsAndConditions;
    private String mobilePhone;
    private String city;
    private String state;
    private String cp;
    private String addressLine1;
    private String addressLine2;
}
