package com.woow.axsalud.data.consultation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class DoctorPrescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String recetaMedica;
    @Column(nullable = true, columnDefinition = "TEXT")
    private String notasDeRecomendaciones;
    @Column(nullable = true, columnDefinition = "TEXT")
    private String diagnostico;

    @ManyToOne
    @JoinColumn(name = "consultation_session_id")
    private ConsultationSession consultationSession;
}
