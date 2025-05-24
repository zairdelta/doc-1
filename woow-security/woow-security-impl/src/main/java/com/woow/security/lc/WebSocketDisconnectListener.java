package com.woow.security.lc;


import com.woow.security.rabbitmq.RabbitMQAdminClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@Slf4j
public class WebSocketDisconnectListener {

    @Value("${stomp.broker.user-queue-prefix:messages-user}")
    private String userQueuePrefix;

    private RabbitMQAdminClient rabbitMQAdminClient;


    private WebSocketDisconnectListener(RabbitMQAdminClient rabbitMQAdminClient) {
        this.rabbitMQAdminClient = rabbitMQAdminClient;

    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        log.debug("🔌 WebSocket session disconnected: " + sessionId);

        // default messages-user<sessionId>
        String queueName = userQueuePrefix + sessionId;
        try {
            rabbitMQAdminClient.deleteQueue(queueName);
        } catch (Exception e) {
            log.error("Error while trying to delete queue: {}, error: {}",
                    queueName, e);
        }

    }
}
