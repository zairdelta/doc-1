package com.woow.axsalud.data.consultation;

import com.woow.axsalud.data.client.AxSaludWooUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
public class Consultation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private UUID consultationId = UUID.randomUUID();
    @OneToMany(mappedBy = "consultation")
    private Set<ConsultationMessageEntity> messages = new HashSet<>();
    @ManyToOne
    @JoinColumn(name = "patient_id")
    private AxSaludWooUser patient;
    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private AxSaludWooUser doctor;
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;
    private LocalDateTime startedAt;
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String symptoms;
    @Enumerated(EnumType.STRING)
    private ConsultationStatus status;
    @OneToMany(mappedBy = "consultation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ConsultationDocument> documents = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
