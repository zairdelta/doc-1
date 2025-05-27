package com.woow.axsalud.service.api.dto;

import lombok.Data;

@Data
public class LabPrescriptionViewDTO {
    private LaboratoryPrescriptionDTO laboratoryPrescriptionDTO;
    private DoctorPrescriptionOwnerDTO doctorPrescriptionOwnerDTO;
}
