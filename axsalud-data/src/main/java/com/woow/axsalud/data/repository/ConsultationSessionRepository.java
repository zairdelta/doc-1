package com.woow.axsalud.data.repository;

import com.woow.axsalud.data.consultation.ConsultationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConsultationSessionRepository extends JpaRepository<ConsultationSession, Long> {
    ConsultationSession findByConsultationSessionId(UUID consultationSessionId);
}
