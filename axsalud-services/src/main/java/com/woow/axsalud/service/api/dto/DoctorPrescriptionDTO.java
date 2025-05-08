package com.woow.axsalud.service.api.dto;

import lombok.Data;

@Data
public class DoctorPrescriptionDTO {
    private String recetaMedica;
    private String notasDeRecomendaciones;
    private String diagnostico;
    private String comentariosMedicos;
}
