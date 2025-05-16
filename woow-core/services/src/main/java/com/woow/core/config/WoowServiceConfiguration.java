package com.woow.core.config;

import com.woow.core.data.user.WoowUser;
import com.woow.core.service.api.UserUpdateDto;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class WoowServiceConfiguration {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Global config to only map matching fields
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setAmbiguityIgnored(false)
                .setMatchingStrategy(org.modelmapper.convention.MatchingStrategies.STRICT);

        modelMapper.typeMap(UserUpdateDto.class, WoowUser.class)
                .addMappings(mapper -> {
                    mapper.skip(WoowUser::setUserId);
                    mapper.skip(WoowUser::setPassword);
                });


        return modelMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
