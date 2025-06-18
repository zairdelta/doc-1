package com.woow.axsalud.data.repository;

import com.woow.axsalud.data.serviceprovider.LocalServiceProviderUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LocalServiceProviderUserRepository
        extends JpaRepository<LocalServiceProviderUserEntity, Long> {
    @Query("SELECT u FROM LocalServiceProviderUserEntity u WHERE u.serviceProviderId = :serviceProviderId AND u.hid = :hid")
    LocalServiceProviderUserEntity findByServiceProviderIdAndHid(long serviceProviderId, String hid);

}
