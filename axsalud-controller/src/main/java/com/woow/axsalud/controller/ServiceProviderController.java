package com.woow.axsalud.controller;

import com.woow.axsalud.service.api.ServiceProviderService;
import com.woow.axsalud.service.api.dto.ServiceProviderDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/serviceprovider")
@Validated
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Service Provider", description = "Operations related to the ServiceProvider registered. needed to create a new user")

public class ServiceProviderController {

    @Autowired
    private ServiceProviderService serviceProviderService;
    @GetMapping
    public ResponseEntity<List<ServiceProviderDTO>> getAllServiceProvider() {
        return ResponseEntity.ok(serviceProviderService.getAllServiceProvider());
    }
}
