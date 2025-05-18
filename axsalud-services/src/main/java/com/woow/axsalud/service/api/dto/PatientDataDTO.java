package com.woow.axsalud.service.api.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PatientDataDTO {
    private String emergencyContactName = " ";
    private String emergencyContactNumber = " ";
    private float height = 0;
    private float weight = 0;
    private float build = 0;
    private List<PatientAdditionalDTO>
            patientAdditionalSet = new ArrayList<>();

    private String preexistences;
    private String occupation;
    private String diseases;
    private String allergies;
    private String surgery;
    private String hospitalized;
    private String medicalTreatment;
    private String medications;
    private String supplements;
    private String smoke;
    private String alcohol;
    private String physicalActivity;
    private String feeding;
    private String hoursYouSleep;
}
