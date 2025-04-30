package com.woow.axsalud.service.api.dto;

import lombok.Data;

@Data
public class PatientDataUpdateDTO {
    private String emergencyContactName = " ";
    private String emergencyContactNumber = " ";
    private float height = 0;
    private float weight = 0;
    private float build = 0;
    private String preexistences;
}
