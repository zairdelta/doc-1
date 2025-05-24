package com.woow.security;

import com.woow.security.interceptor.JwtWebSocketInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSocketInterceptorConfig {

    @Bean
    public JwtWebSocketInterceptor jwtWebSocketInterceptor() {
        return new JwtWebSocketInterceptor();
    }
}
