package com.woow.axsalud.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorDataRepository extends JpaRepository<com.woow.axsalud.data.client.DoctorData, Long> {
}
