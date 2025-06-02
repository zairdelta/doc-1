package com.woow.axsalud.data.repository;

import com.woow.axsalud.data.consultation.ConsultationSession;
import com.woow.axsalud.data.consultation.ConsultationSessionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ConsultationSessionRepository extends JpaRepository<ConsultationSession, Long> {
    ConsultationSession findByConsultationSessionId(UUID consultationSessionId);

    //Needed to update the status of the consultation when handshaking, to avoid dirty reads and have conditions
    // for example when patient and doctor are ready, but just one got the state ready in the DB
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ConsultationSession c WHERE c.consultationSessionId = :sessionId")
    ConsultationSession findWithLock(@Param("sessionId") UUID sessionId);

    @Query("SELECT c FROM ConsultationSession c " +
            "WHERE c.doctorLastTimePing <= :doctorLastTimePing " +
            "AND c.status IN :statuses")
    List<ConsultationSession> findByDoctorLastTimeSeen(
            @Param("doctorLastTimePing") LocalDateTime doctorLastTimePing,
            @Param("statuses") List<ConsultationSessionStatus> statuses);

    @Query("SELECT c FROM ConsultationSession c " +
            "WHERE c.doctorLastTimePing <= :patientLastTimePing " +
            "AND c.status IN :statuses")
    List<ConsultationSession> findByPatientLastTimeSeen(
            @Param("patientLastTimePing") LocalDateTime patientLastTimePing,
            @Param("statuses") List<ConsultationSessionStatus> statuses);

    @Modifying
    @Query("UPDATE ConsultationSession c SET c.doctorLastTimePing = :time WHERE c.consultationSessionId = :sessionId")
    void updateDoctorLastPing(@Param("sessionId") UUID sessionId, @Param("time") LocalDateTime time);

    @Modifying
    @Query("UPDATE ConsultationSession c SET c.patientLastTimePing = :time WHERE c.consultationSessionId = :sessionId")
    void updatePatientLastPing(@Param("sessionId") UUID sessionId, @Param("time") LocalDateTime time);

}
