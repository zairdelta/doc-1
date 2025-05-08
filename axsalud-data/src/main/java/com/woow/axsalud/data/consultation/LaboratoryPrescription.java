package com.woow.axsalud.data.consultation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class LaboratoryPrescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String ordenDeLaboratorio;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String notasDeRecomendaciones;
    //interno
    @Column(nullable = false, columnDefinition = "TEXT")
    private String posibleDiagnostico;
    // interno
    @Column(nullable = false, columnDefinition = "TEXT")
    private String observacionesMedicas;

    @ManyToOne
    @JoinColumn(name = "consultation_session_id")
    private ConsultationSession consultationSession;

}
