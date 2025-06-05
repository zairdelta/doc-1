package com.woow.axsalud.service.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DoctorCommentsDTO {
    private String comment;
    private String doctorFullName;
    private LocalDateTime createdAt;
}
