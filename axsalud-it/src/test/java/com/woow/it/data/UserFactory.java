package com.woow.it.data;

import com.woow.axsalud.data.client.PatientData;
import com.woow.axsalud.service.api.dto.AxSaludUserDTO;
import com.woow.core.service.api.UserDtoCreate;

import java.util.Date;

public class UserFactory {

    public static AxSaludUserDTO anyUser() {
        UserDtoCreate user = new UserDtoCreate();
        user.setUserName("realuser@woow.com");
        user.setPassword("realpassword");
        user.setName("Real");
        user.setLastName("User");
        user.setEmail("realuser@woow.com");
        user.setBirth(new Date());
        user.setCountry("MX");
        user.setServiceProvider("HealthConnect");
        user.setAcceptTermsAndConditions("yes");
        user.setMobilePhone("1234567890");
        user.setCity("CDMX");
        user.setState("CDMX");
        user.setCp("12345");
        user.setAddressLine1("Street 1");
        user.setAddressLine2("Street 2");

        PatientData patientData = new PatientData();
        patientData.setBuild(12.1f);
        patientData.setEmergencyContactName("ENAME");
        patientData.setEmergencyContactNumber("123123");

        AxSaludUserDTO dto = new AxSaludUserDTO();

        dto.setUserDtoCreate(user);
        dto.setHid("HID-123");
        return dto;
    }

    public static AxSaludUserDTO master() {
        UserDtoCreate user = new UserDtoCreate();
        user.setUserName("master@example.com");
        user.setPassword("realpassword");
        user.setName("Real");
        user.setLastName("User");
        user.setEmail("master@example.com");
        user.setBirth(new Date());
        user.setCountry("MX");
        user.setServiceProvider("HealthConnect");
        user.setAcceptTermsAndConditions("yes");
        user.setMobilePhone("1234567890");
        user.setCity("CDMX");
        user.setState("CDMX");
        user.setCp("12345");
        user.setAddressLine1("Street 1");
        user.setAddressLine2("Street 2");

        AxSaludUserDTO dto = new AxSaludUserDTO();
        dto.setUserDtoCreate(user);
        dto.setHid("HID-123");
        return dto;
    }

}
