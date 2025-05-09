package com.woow.axsalud.service.api.dto;

import com.woow.core.service.api.UserUpdateDto;
import lombok.Data;
@Data
public class HealthServiceProviderUpdateDTO {
 private UserUpdateDto userUpdateDto;
    private String welcomeMessage;
    private String dni;
    private DoctorDataDTO doctorData;
}
