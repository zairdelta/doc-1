package com.woow.axsalud.data.repository;

import com.woow.axsalud.data.consultation.ConsultationSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConsultationSessionRepository extends JpaRepository<ConsultationSession, Long> {
    ConsultationSession findByConsultationSessionId(UUID consultationSessionId);

    //Needed to update the status of the consultation when handshaking, to avoid dirty reads and have conditions
    // for example when patient and doctor are ready, but just one got the state ready in the DB
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ConsultationSession c WHERE c.consultationSessionId = :sessionId")
    ConsultationSession findWithLock(@Param("sessionId") UUID sessionId);
}
