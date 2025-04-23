package com.woow.axsalud.service.api.dto;

import com.woow.axsalud.data.client.DoctorData;
import com.woow.core.service.api.UserDtoCreate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HealthServiceProviderDTO {
    private UserDtoCreate userDtoCreate;
    private String welcomeMessage;
    private DoctorData doctorData;
}
