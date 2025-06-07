package com.woow.security.lc;

import com.woow.security.rabbitmq.RabbitMQAdminClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final RabbitMQAdminClient rabbitMQAdminClient;
    private final WSCache wsCache;

    @Autowired
    public WebSocketDisconnectListener(RabbitMQAdminClient rabbitMQAdminClient, WSCache wsCache) {
        this.rabbitMQAdminClient = rabbitMQAdminClient;
        this.wsCache = wsCache;
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        log.debug("🔌 WebSocket session disconnected: {}", sessionId);

        WSCacheInput removed = wsCache.getSession(sessionId);
        if (removed != null) {
            wsCache.removeSession(sessionId);
            log.debug("🧹 Removed session from WSCache: sessionId={}, username={}", sessionId, removed.getUsername());
        } else {
            log.warn("⚠️ No cache entry found for session: {}", sessionId);
        }

        String queueName = userQueuePrefix + sessionId;
        try {
            rabbitMQAdminClient.deleteQueue(queueName);
            log.debug("🗑️ Deleted RabbitMQ queue: {}", queueName);
        } catch (Exception e) {
            log.error("❌ Error deleting queue: {}, error: {}", queueName, e.getMessage(), e);
        }
    }
}
