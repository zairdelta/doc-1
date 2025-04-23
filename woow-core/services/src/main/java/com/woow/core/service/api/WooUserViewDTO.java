package com.woow.core.service.api;

import lombok.Data;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
public class WooUserViewDTO {
    private long userId;
    private Date birth;
    private String name;
    private String userName;
    private String lastName;
    private String country;
    private String email;
    private String serviceProvider;
    private String serviceId;
    private String acceptTermsAndConditions;
    private String mobilePhone;
    private String city;
    private String state;
    private String cp;
    private String addressLine1;
    private String addressLine2;
    private boolean userActive;
    private LocalDate createdAt;
    private Set<String> roles = new HashSet<>();
}
