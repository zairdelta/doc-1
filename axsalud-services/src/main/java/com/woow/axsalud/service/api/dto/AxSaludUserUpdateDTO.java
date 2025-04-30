package com.woow.axsalud.service.api.dto;

import com.woow.core.service.api.UserUpdateDto;
import lombok.Data;

@Data
public class AxSaludUserUpdateDTO {
    private UserUpdateDto userUpdateDto;
    private PatientDataUpdateDTO patientDataUpdateDTO;
}
