package com.woow.security.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;

@Component
@Slf4j
public class RabbitMQAdminClient {

    private final RabbitMQStompBrokerProperties stompBrokerProperties;

    public RabbitMQAdminClient(RabbitMQStompBrokerProperties stompBrokerProperties) {
        this.stompBrokerProperties = stompBrokerProperties;
    }

    public void deleteQueue(String queueName) throws Exception {
        log.debug("Deleting queue from RabbitMQ: {}", queueName);
        TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;


        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(stompBrokerProperties.getRelayHost());
        factory.setPort(stompBrokerProperties.getAdminPort());
        factory.setUsername(stompBrokerProperties.getClientLogin());
        factory.setPassword(stompBrokerProperties.getClientPasscode());
        factory.useSslProtocol(sslContext);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDelete(queueName);
            log.debug("Delete queue command send to: {}", stompBrokerProperties.getRelayHost());
        }
    }
}
