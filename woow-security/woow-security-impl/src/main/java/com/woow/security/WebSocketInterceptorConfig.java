package com.woow.security;

import com.woow.security.interceptor.inbound.ConnectWsInterceptor;
import com.woow.security.interceptor.inbound.InboundMessageLoggingWsInterceptor;
import com.woow.security.interceptor.inbound.SendMessageWsInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSocketInterceptorConfig {

    @Bean
    public ConnectWsInterceptor connectWsInterceptor() {
        return new ConnectWsInterceptor();
    }

    @Bean
    public InboundMessageLoggingWsInterceptor inboundMessageLoggingWsInterceptor() {
        return new InboundMessageLoggingWsInterceptor();
    }

    @Bean
    public SendMessageWsInterceptor sendMessageWsInterceptor() {
        return new SendMessageWsInterceptor();
    }
}
