package com.woow.axsalud.data.serviceprovider;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class LocalServiceProviderUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private int userId;
    private String hid;
    private String name;
    private String lastName;
    private long serviceProviderId;
    private int userValid;
    private LocalDateTime createdAt;
}
