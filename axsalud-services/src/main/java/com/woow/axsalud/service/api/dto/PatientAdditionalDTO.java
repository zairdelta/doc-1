package com.woow.axsalud.service.api.dto;

import com.woow.axsalud.data.client.PatientAdditional;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientAdditionalDTO {
    private String name;
    private LocalDate birth;

    public static PatientAdditional from(PatientAdditionalDTO patientAdditionalDTO) {
        PatientAdditional patientAdditional = new PatientAdditional();
        patientAdditional.setName(patientAdditionalDTO.getName());
        patientAdditional.setBirth(patientAdditionalDTO.getBirth());
        return patientAdditional;
    }
}
