package com.woow.security.config;

import com.woow.security.config.interceptor.JwtWebSocketInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSocketInterceptorConfig {

    @Bean
    public JwtWebSocketInterceptor jwtWebSocketInterceptor() {
        return new JwtWebSocketInterceptor();
    }
}
