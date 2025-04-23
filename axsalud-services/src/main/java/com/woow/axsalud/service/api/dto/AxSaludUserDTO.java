package com.woow.axsalud.service.api.dto;

import com.woow.core.service.api.UserDtoCreate;
import lombok.Data;

@Data
public class AxSaludUserDTO {
    private UserDtoCreate userDtoCreate;
    private String hid;
}
