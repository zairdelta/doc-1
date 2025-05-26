package com.woow.axsalud.data.consultation;

import com.woow.axsalud.data.client.AxSaludWooUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
public class ConsultationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID consultationSessionId = UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "consultation_id")
    private Consultation consultation;

    @ManyToOne
    private AxSaludWooUser doctor;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime startAt;
    private LocalDateTime finishedAt;

    @Enumerated(EnumType.STRING)
    private ConsultationSessionStatus status;

    @OneToMany(mappedBy = "consultationSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ConsultationMessageEntity> messages = new HashSet<>();

    @OneToMany(mappedBy = "consultationSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ConsultationDocument> documents = new HashSet<>();

    @ElementCollection
    private Set<String> closedBy = new HashSet<>();

    @OneToMany(mappedBy = "consultationSession",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DoctorPrescription> doctorPrescriptions = new HashSet<>();

    @OneToMany(mappedBy = "consultationSession",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LaboratoryPrescription> laboratoryPrescriptions;

    @OneToOne(mappedBy = "consultationSession")
    private ComentariosMedicos comentariosMedicos;
    @Enumerated(EnumType.STRING)
    private PartyConsultationStatus patientStatus;
    @Enumerated(EnumType.STRING)
    private PartyConsultationStatus doctorStatus;
}
