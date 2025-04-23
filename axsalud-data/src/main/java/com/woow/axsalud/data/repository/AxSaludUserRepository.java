package com.woow.axsalud.data.repository;

import com.woow.axsalud.data.client.AxSaludWooUser;
import com.woow.core.data.user.WoowUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AxSaludUserRepository extends JpaRepository<AxSaludWooUser, Long> {
    Optional<AxSaludWooUser> findByCoreUser(WoowUser coreUser);
    Optional<AxSaludWooUser> findByCoreUser_UserId(long userId);
}
