package com.woow.axsalud.data.consultation;

import com.woow.axsalud.data.client.AxSaludWooUser;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@Entity
public class ConsultationMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "consultation_session_id")
    private ConsultationSession consultationSession;
    @ManyToOne
    @JoinColumn(name = "sent_by_user_id", nullable = false)
    private AxSaludWooUser sentBy;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Lob
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private ConsultationMessageStatus status;
    private String messageType;

}
