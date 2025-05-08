package com.woow.axsalud.data.consultation;

import com.woow.axsalud.data.client.AxSaludWooUser;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ComentariosMedicos {

    @Id
    @GeneratedValue
    private long id;
    @Column(nullable = true, columnDefinition = "TEXT")
    private String observacionesMedicas;

    @ManyToOne
    @JoinColumn(name = "ax_salud_user_id")
    private AxSaludWooUser axSaludWooUser;
    @OneToOne
    @JoinColumn(name = "consultation_session_id")
    private ConsultationSession consultationSession;
}
