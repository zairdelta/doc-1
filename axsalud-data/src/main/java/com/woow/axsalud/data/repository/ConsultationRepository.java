package com.woow.axsalud.data.repository;

import com.woow.axsalud.data.consultation.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    Consultation findByConsultationId(UUID uuid);
    @Query("SELECT c FROM Consultation c ORDER BY CASE WHEN c.status = 'WAITING_FOR_DOCTOR' THEN 0 ELSE 1 END, c.createdAt ASC")
    List<Consultation> findAllOrderByWaitingForDoctorFirst();
}
