package com.woow.security.config;

import com.woow.security.api.JwtTokenUtil;
import com.woow.security.config.interceptor.JwtWebSocketInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    private final JwtWebSocketInterceptor jwtWebSocketInterceptor;

    public WebSocketConfig(JwtWebSocketInterceptor jwtWebSocketInterceptor) {
        this.jwtWebSocketInterceptor = jwtWebSocketInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request,
                                                      WebSocketHandler wsHandler,
                                                      Map<String, Object> attributes) {


                        log.info("DetermineUser getting request");
                        HttpHeaders headers = request.getHeaders();
                        headers.forEach((key, value) -> {
                            log.info("Header '{}' = {}", key, value);
                        });

                        if (request.getHeaders().get("Authorization") == null) {
                            return null;

                        } else {

                            String authHeader = request.getHeaders().get("Authorization").get(0);
                            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                                String jwtToken = authHeader.substring(7);
                                String username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                                return () -> username;
                            }

                            return null;

                }}})
                .setAllowedOriginPatterns("*");
                //.withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/queue", "/topic");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtWebSocketInterceptor);
    }
}
