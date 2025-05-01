package com.woow.axsalud.service.api.dto;

import com.woow.axsalud.data.client.DoctorData;
import com.woow.core.service.api.UserUpdateDto;
import lombok.Data;
@Data
public class HealthServiceProviderUpdateDTO {
 private UserUpdateDto userUpdateDto;
    private String welcomeMessage;
    private DoctorDataDTO doctorData;
}
