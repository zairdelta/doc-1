package com.woow.axsalud.data.repository;

import com.woow.axsalud.data.consultation.DoctorPrescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorPrescriptionRepository extends JpaRepository<DoctorPrescription, Long> {

    @Query("""
        SELECT dp FROM DoctorPrescription dp
        WHERE dp.consultationSession.doctor.coreUser.userName = :userName
    """)
    List<DoctorPrescription> findAllByDoctorUserName(@Param("userName") String userName);

    @Query("""
        SELECT dp FROM DoctorPrescription dp
        WHERE dp.consultationSession.consultation.patient.coreUser.userName = :userName
    """)
    List<DoctorPrescription> findAllByPatientUserName(@Param("userName") String userName);
}
