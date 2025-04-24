package com.woow.axsalud.data.repository;

import com.woow.axsalud.data.consultation.ConsultationDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultationDocumentRepository extends JpaRepository<ConsultationDocument, Long> {
}
