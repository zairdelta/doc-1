package com.woow.axsalud.service.api.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class LaboratoryPrescriptionDTO {
    private long id;
    private String ordenDeLaboratorio;
    private String notasDeRecomendaciones;
    private String posibleDiagnostico;
    private String observacionesMedicas;
}
