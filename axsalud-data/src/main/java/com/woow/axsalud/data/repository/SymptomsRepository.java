package com.woow.axsalud.data.repository;

import com.woow.axsalud.data.client.Symptoms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SymptomsRepository extends JpaRepository<Symptoms, Long> {
}
