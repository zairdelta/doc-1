package com.woow.axsalud.data.repository;

import com.woow.axsalud.common.UserStatesEnum;
import com.woow.axsalud.data.client.AxSaludWooUser;
import com.woow.core.data.user.WoowUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AxSaludUserRepository extends JpaRepository<AxSaludWooUser, Long> {
    Optional<AxSaludWooUser> findByCoreUser(WoowUser coreUser);
    Optional<AxSaludWooUser> findByCoreUser_UserId(long userId);

    Optional<AxSaludWooUser> findByHid(String hid);

    @Query("""
    SELECT COUNT(u)
    FROM AxSaludWooUser u
    JOIN u.coreUser.roles r
    WHERE r = 'DOCTOR'
      AND u.userAvailability = :state
""")
    int countOnlineDoctors(@Param("state") UserStatesEnum state);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE AxSaludWooUser u SET u.userAvailability = :newState WHERE u.coreUser.userName = :userName")
    int updateUserStateByCoreUserId(@Param("userName") String userName, @Param("newState") UserStatesEnum newState);
}
