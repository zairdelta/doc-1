package com.woow.axsalud.data.repository;

import com.woow.axsalud.data.consultation.LaboratoryPrescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaboratoryPrescriptionsRepository extends JpaRepository<LaboratoryPrescription, Long> {

    @Query("""
        SELECT lp FROM LaboratoryPrescription lp
        WHERE lp.consultationSession.doctor.coreUser.userName = :userName
    """)
    List<LaboratoryPrescription> findAllByDoctorUserName(@Param("userName") String userName);
}


