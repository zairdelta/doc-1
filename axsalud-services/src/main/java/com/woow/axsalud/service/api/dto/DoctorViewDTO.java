package com.woow.axsalud.service.api.dto;

import com.woow.axsalud.data.client.DoctorData;
import lombok.Data;

@Data
public class DoctorViewDTO extends AxSaludUserViewDTO {
    private DoctorData doctorData;
    private String welcomeMessage;
}
