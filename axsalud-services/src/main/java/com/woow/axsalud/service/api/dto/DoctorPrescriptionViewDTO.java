package com.woow.axsalud.service.api.dto;

import lombok.Data;

@Data
public class DoctorPrescriptionViewDTO {
    private DoctorPrescriptionDTO doctorPrescriptionDTO;
    private DoctorPrescriptionOwnerDTO doctorPrescriptionOwnerDTO;
 }
