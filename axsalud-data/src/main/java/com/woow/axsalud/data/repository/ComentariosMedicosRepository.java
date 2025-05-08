package com.woow.axsalud.data.repository;

import com.woow.axsalud.data.consultation.ComentariosMedicos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentariosMedicosRepository extends JpaRepository<ComentariosMedicos, Long> {
    List<ComentariosMedicos> findByAxSaludWooUser_CoreUser_UserName(String userName);
}
