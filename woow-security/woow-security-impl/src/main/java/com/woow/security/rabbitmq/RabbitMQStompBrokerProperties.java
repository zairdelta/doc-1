package com.woow.security.rabbitmq;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "stomp.broker")
@Data
public class RabbitMQStompBrokerProperties {
    private String relayHost;
    private int relayPort;
    private int adminPort;
    private String clientLogin;
    private String clientPasscode;
    private String systemLogin;
    private String systemPasscode;
    private int maxConnections;
    private int pendingAcquireMaxCount;
    private int pendingAcquireTimeoutInSeconds;
    private String connectionPoolName;
}
