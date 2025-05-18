package com.woow.axsalud.service.api.dto;

import com.woow.axsalud.data.client.PatientData;
import lombok.Data;

@Data
public class PatientViewDTO extends AxSaludUserViewDTO {
    private String userType;
    private PatientDataDTO patientDataDTO;
}
