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
    private Set<PatientAdditional> patientAdditionalSet = new HashSet<>();

    @Column(nullable = false, columnDefinition = "TEXT")
    private String preexistences;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String occupation;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String diseases;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String allergies;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String surgery;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String hospitalized;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String medicalTreatment;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String medications;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String supplements;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String smoke;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String alcohol;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String physicalActivity;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String feeding;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String hoursYouSleep;
}
