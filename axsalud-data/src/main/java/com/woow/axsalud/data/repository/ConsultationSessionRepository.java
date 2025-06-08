package com.woow.axsalud.data.repository;

import com.woow.axsalud.data.consultation.ConsultationSession;
import com.woow.axsalud.data.consultation.ConsultationSessionStatus;
import com.woow.axsalud.data.consultation.PartyConsultationStatus;
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
            "WHERE c.patientLastTimePing <= :patientLastTimePing " +
            "AND c.status IN :statuses")
    List<ConsultationSession> findByPatientLastTimeSeen(
            @Param("patientLastTimePing") LocalDateTime patientLastTimePing,
            @Param("statuses") List<ConsultationSessionStatus> statuses);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ConsultationSession c SET c.doctorLastTimePing = :time WHERE c.consultationSessionId = :sessionId")
    int updateDoctorLastPing(@Param("sessionId") UUID sessionId, @Param("time") LocalDateTime time);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ConsultationSession c SET c.patientLastTimePing = :time WHERE c.consultationSessionId = :sessionId")
    int updatePatientLastPing(@Param("sessionId") UUID sessionId, @Param("time") LocalDateTime time);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ConsultationSession c SET c.status = :status WHERE c.consultationSessionId = :sessionId")
    int updateStatus(@Param("sessionId") UUID sessionId, @Param("status") ConsultationSessionStatus status);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ConsultationSession c SET c.patientStatus = :status WHERE c.consultationSessionId = :sessionId")
    int updatePatientStatus(@Param("sessionId") UUID sessionId, @Param("status") PartyConsultationStatus status);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ConsultationSession c SET c.doctorStatus = :status WHERE c.consultationSessionId = :sessionId")
    int updateDoctorStatus(@Param("sessionId") UUID sessionId, @Param("status") PartyConsultationStatus status);
}
