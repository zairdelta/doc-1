package com.woow.core.data.repository;

import com.woow.core.data.user.WoowUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WoowUserRepository extends JpaRepository<WoowUser, Long> {

    WoowUser findByEmail(String email);
    WoowUser findByUserName(String email);
    WoowUser findByUserId(long userId);

}
