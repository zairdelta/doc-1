package com.woow.axsalud.service.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DoctorPrescriptionOwnerDTO {
    private LocalDateTime createdAt;
    private String doctorFullName;
    private String doctorDNI;
    private String doctorEmail;
}
