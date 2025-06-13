package com.woow.axsalud.service.api.dto;

import lombok.Data;

@Data
public class HealthServiceProviderViewDTO extends AxSaludUserViewDTO {
    private String welcomeMessage;
    private DoctorDataDTO doctorData;
}
