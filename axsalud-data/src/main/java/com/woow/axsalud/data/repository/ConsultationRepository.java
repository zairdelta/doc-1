package com.woow.axsalud.data.repository;

import com.woow.axsalud.data.consultation.Consultation;
import com.woow.axsalud.data.consultation.ConsultationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long>,
        PagingAndSortingRepository<Consultation, Long> {
    Consultation findByConsultationId(UUID uuid);
    @Query("SELECT c FROM Consultation c ORDER BY CASE WHEN c.status = 'WAITING_FOR_DOCTOR' THEN 0 ELSE 1 END, c.createdAt ASC")
    List<Consultation> findAllOrderByWaitingForDoctorFirst();
    List<Consultation> findByStatusOrderByCreatedAtAsc(ConsultationStatus status);

    @Query("""
        SELECT 
            c.id AS id,
            c.consultationId AS consultationId,
            s.consultationSessionId AS consultationSessionId,
            CONCAT(d.coreUser.name, ' ', d.coreUser.lastName) AS doctorName,
            c.symptoms AS symptoms,
            c.status AS status,
            c.createdAt AS createdAt
        FROM Consultation c
        LEFT JOIN c.sessions s
        LEFT JOIN s.doctor d
        WHERE c.patient.id = :patientId
    """)
    Page<PatientConsultationSummary> findConsultationsByPatientId(Long patientId, Pageable pageable);


    @Modifying(clearAutomatically = true)
    @Query("UPDATE Consultation c SET c.status = :status WHERE c.consultationId = :consultationId")
    int updateStatus(@Param("consultationId") UUID consultationId, @Param("status") ConsultationStatus status);

}
