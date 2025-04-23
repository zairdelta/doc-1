package com.woow.axsalud.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.modelmapper.ModelMapper;

@Configuration
public class AxSaludServicesConfiguration {

    @Bean
    public ModelMapper modelMapperBean() {
        return new ModelMapper();
    }
}
