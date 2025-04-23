package com.woow.axsalud.data.client;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class PatientData {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long id;
    private String emergencyContactName = " ";
    private String emergencyContactNumber = " ";
    private float height = 0;
    private float weight = 0;
    private float build = 0;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "patient_data_id")
    Set<PatientAdditional> patientAdditionalSet = new HashSet<>();
}
