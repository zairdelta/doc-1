package com.woow.axsalud.data.repository;

import com.woow.axsalud.data.client.PatientData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientDataRepository extends JpaRepository<PatientData, Long> {
}
