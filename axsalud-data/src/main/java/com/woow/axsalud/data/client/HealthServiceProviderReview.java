package com.woow.axsalud.data.client;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class HealthServiceProviderReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int totalStarts;
    @Lob
    @Column(nullable = true, columnDefinition = "TEXT")
    private String text;
    private int serviceType;
}
