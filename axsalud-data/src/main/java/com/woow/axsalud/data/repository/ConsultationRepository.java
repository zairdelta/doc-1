package com.woow.axsalud.data.repository;

import com.woow.axsalud.data.consultation.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    Consultation findByConsultationId(UUID uuid);
    //List<Consultation> findAllOrderByStatusDesc();
}
