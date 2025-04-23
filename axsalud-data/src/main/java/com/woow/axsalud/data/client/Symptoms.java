package com.woow.axsalud.data.client;

import com.woow.axsalud.data.consultation.Consultation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Symptoms {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long id;
    @Lob
    private String symptom;
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "consultation_id")
    private Consultation consultation;

}
