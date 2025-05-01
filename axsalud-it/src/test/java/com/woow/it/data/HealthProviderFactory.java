package com.woow.it.data;

import com.woow.axsalud.data.client.DoctorData;
import com.woow.axsalud.service.api.dto.DoctorDataDTO;
import com.woow.axsalud.service.api.dto.HealthServiceProviderDTO;
import com.woow.core.service.api.UserDtoCreate;

import java.util.Date;

public class HealthProviderFactory {
    public static HealthServiceProviderDTO anyHealthProvider() {
        UserDtoCreate user = new UserDtoCreate();
        user.setUserName("doctor@woow.com");
        user.setPassword("realpassword");
        user.setName("Real");
        user.setLastName("User");
        user.setEmail("doctor@woow.com");
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

        DoctorDataDTO doctorData = new DoctorDataDTO();
        doctorData.setSpeciality("Speciality");
        doctorData.setUniversity("University");
        doctorData.setLicenseNumber("number 00012324ODF");

        HealthServiceProviderDTO dto = new HealthServiceProviderDTO();
        dto.setDoctorData(doctorData);
        dto.setUserDtoCreate(user);
        dto.setWelcomeMessage("WELCOME");
        return dto;
    }

    public static HealthServiceProviderDTO anyDoctor() {
        UserDtoCreate user = new UserDtoCreate();
        user.setUserName("doctor@woow.com");
        user.setPassword("realpassword");
        user.setName("Doctor");
        user.setLastName("Doctor");
        user.setEmail("doctor@woow.com");
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

        HealthServiceProviderDTO dto = new HealthServiceProviderDTO();
        dto.setUserDtoCreate(user);
        return dto;
    }
}
