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
        doctorDataDTO.setMatriculaNacional(doctorData.getMatriculaNacional());
        doctorDataDTO.setMatriculaProvincial(doctorData.getMatriculaProvincial());
        return doctorDataDTO;
    }

    public static DoctorData from(DoctorDataDTO doctorDataDTO) {
        DoctorData doctorData = new DoctorData();
        doctorData.setUniversity(doctorDataDTO.getUniversity());
        doctorData.setLicenseNumber(doctorDataDTO.getLicenseNumber());
        doctorData.setSpeciality(doctorDataDTO.getSpeciality());
        doctorData.setMatriculaNacional(doctorDataDTO.getMatriculaNacional());
        doctorData.setMatriculaProvincial(doctorDataDTO.getMatriculaProvincial());
        return doctorData;
    }
}
