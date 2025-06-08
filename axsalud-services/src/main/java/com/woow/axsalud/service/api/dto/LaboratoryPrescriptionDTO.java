package com.woow.axsalud.service.api.dto;

import lombok.Data;

@Data
public class LaboratoryPrescriptionDTO {
    private long id;
    private String ordenDeLaboratorio;
    private String notasDeRecomendaciones;
    private String posibleDiagnostico;
    private String observacionesMedicas;
}
