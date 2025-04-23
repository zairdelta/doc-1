package com.woow.security.api;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BlackListRepository extends JpaRepository<JwtBlackList, Integer> {

    @Query(value = "Select bl from JwtBlackList bl where bl.token = :token")
    JwtBlackList findbyToken(@Param("token") String token);
}
