package com.woow.axsalud.service.api.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AxSaludUserViewDTO {
    private LocalDate birth;
    private String name;
    private String userName;
    private String lastName;
    private String country;
    private String email;
    private String serviceProvider;
    private String acceptTermsAndConditions;
    private String mobilePhone;
    private String city;
    private String state;
    private String cp;
    private String addressLine1;
    private String addressLine2;
    private String hid;
}