package com.woow.axsalud.data.client;

import com.woow.axsalud.common.UserStatesEnum;
import com.woow.axsalud.data.consultation.ComentariosMedicos;
import com.woow.axsalud.data.consultation.Consultation;
import com.woow.core.data.user.WoowUser;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Data
public class AxSaludWooUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private WoowUserType userType;
    @Enumerated(EnumType.STRING)
    private CountryLocationOffices locationOffices;
    @OneToMany(mappedBy = "patient")
    private List<Consultation> patientConsultations = new ArrayList<>();
    @OneToOne
    @JoinColumn(name = "core_user_id", referencedColumnName = "userId")
    private WoowUser coreUser;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "doctor_data_user_id", referencedColumnName = "id")
    private DoctorData doctorData;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "patient_data_user_id", referencedColumnName = "id")
    private PatientData patientData;
    @Enumerated(EnumType.STRING)
    private UserStatesEnum state = UserStatesEnum.OFFLINE;
    private long serviceProvider;
    private String hid;
    private String doctorWelcomeMessage;

    @OneToMany(mappedBy = "axSaludWooUser")
    private Set<ComentariosMedicos> comentariosMedicos;
}
