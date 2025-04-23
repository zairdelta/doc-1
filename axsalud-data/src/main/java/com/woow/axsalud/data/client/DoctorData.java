package com.woow.axsalud.data.client;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class DoctorData {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long id;
    private String licenseNumber;
    private String speciality;
    private String university;
}
