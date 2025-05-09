package com.woow.axsalud.service.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HealthServiceProviderDTO extends AxSaludUserDTO {
    private String welcomeMessage;
    private DoctorDataDTO doctorData;

}
