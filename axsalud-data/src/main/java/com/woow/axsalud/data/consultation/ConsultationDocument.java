package com.woow.axsalud.data.consultation;

import com.woow.axsalud.data.client.AxSaludWooUser;
import com.woow.axsalud.data.client.WoowUserType;
import com.woow.axsalud.data.consultation.Consultation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ConsultationDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String fileType;
    private String elementPublicId;
    private String secureUrl;
    private String format;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastAccessedAt;
    private LocalDateTime lastModifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id")
    private AxSaludWooUser uploadedBy;

    @Enumerated(EnumType.STRING)
    private WoowUserType uploaderRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id")
    private Consultation consultation;
}

