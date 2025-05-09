package com.woow.axsalud.service.api.dto;

import com.woow.axsalud.data.client.DoctorData;
import lombok.Data;

@Data
public class DoctorDataDTO {
    private String licenseNumber;
    private String speciality;
    private String university;
    private String matriculaNacional;
    private String matriculaProvincial;

    public static DoctorDataDTO from(DoctorData doctorData) {
        DoctorDataDTO doctorDataDTO = new DoctorDataDTO();
        doctorDataDTO.setUniversity(doctorData.getUniversity());
        doctorDataDTO.setLicenseNumber(doctorData.getLicenseNumber());
        doctorDataDTO.setSpeciality(doctorData.getSpeciality());
        doctorDataDTO.setMatriculaNacional(doctorDataDTO.getMatriculaNacional());
        doctorDataDTO.setMatriculaProvincial(doctorDataDTO.getMatriculaProvincial());
        return doctorDataDTO;
    }
}
