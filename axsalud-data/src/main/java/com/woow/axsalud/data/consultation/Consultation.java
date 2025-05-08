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

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private AxSaludWooUser patient;

    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;
    private LocalDateTime startedAt;
    @Lob
    @Column(nullable = true, columnDefinition = "TEXT")
    private String symptoms;
    @Enumerated(EnumType.STRING)
    private ConsultationStatus status;
    private String currentSessionIdIfExists;

    @OneToMany(mappedBy = "consultation",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConsultationSession> sessions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
