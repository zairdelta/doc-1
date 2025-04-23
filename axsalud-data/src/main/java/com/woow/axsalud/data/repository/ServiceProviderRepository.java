package com.woow.axsalud.data.repository;

import com.woow.axsalud.data.serviceprovider.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {
    ServiceProvider findByName(final String name);
}
