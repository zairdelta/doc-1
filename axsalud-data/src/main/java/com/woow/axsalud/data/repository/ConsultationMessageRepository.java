package com.woow.axsalud.data.repository;

import com.woow.axsalud.data.consultation.ConsultationMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultationMessageRepository extends JpaRepository<ConsultationMessageEntity, Long> {
}
