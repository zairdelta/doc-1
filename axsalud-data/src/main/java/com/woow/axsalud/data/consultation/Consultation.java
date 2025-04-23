package com.woow.axsalud.data.consultation;

import com.woow.axsalud.data.client.AxSaludWooUser;
import com.woow.axsalud.data.client.Symptoms;
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
    @OneToMany(mappedBy = "consultation")
    private List<Symptoms> symptoms = new ArrayList<>();
    @Enumerated(EnumType.STRING)
    private ConsultationStatus status;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
