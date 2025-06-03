package com.woow.axsalud.data.repository;

import com.woow.axsalud.data.consultation.ConsultationMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConsultationMessageRepository extends JpaRepository<ConsultationMessageEntity, Long> {

    @Query("""
    SELECT m
    FROM ConsultationMessageEntity m
    WHERE m.consultationSession.consultation.patient.coreUser.userName = :userName
    ORDER BY m.timestamp DESC
""")
    Page<ConsultationMessageEntity> findMessagesByPatientUserNameOrdered(@Param("userName") String userName,
                                                                         Pageable pageable);

    @Query("""
    SELECT m
    FROM ConsultationMessageEntity m
    WHERE m.consultationSession.consultationSessionId = :sessionId
    ORDER BY m.timestamp ASC
""")
    Page<ConsultationMessageEntity> findMessagesByConsultationSessionId(@Param("sessionId") UUID sessionId,
                                                                        Pageable pageable);


}
