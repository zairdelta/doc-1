package com.woow.security.lc;

import com.woow.security.api.ws.StompDisconnectAppEvent;
import com.woow.security.api.ws.WSCacheInput;
import com.woow.security.rabbitmq.RabbitMQAdminClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@Slf4j
public class WebSocketDisconnectListener {

    @Value("${stomp.broker.user-queue-prefix:messages-user}")
    private String userQueuePrefix;
    @Autowired
    private ApplicationEventPublisher publisher;

    private final RabbitMQAdminClient rabbitMQAdminClient;
    private final WSCache wsCache;

    public WebSocketDisconnectListener(RabbitMQAdminClient rabbitMQAdminClient, WSCache wsCache) {
        this.rabbitMQAdminClient = rabbitMQAdminClient;
        this.wsCache = wsCache;
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        log.debug("🔌{}_ WebSocket session disconnected: {}", sessionId, sessionId);

        WSCacheInput removed = wsCache.getSession(sessionId);
        if (removed != null) {
            wsCache.removeSession(sessionId);
            publisher.publishEvent(new StompDisconnectAppEvent(this, removed));
            log.debug("🧹 {}_ Removed session from WSCache: sessionId={}, username={}", sessionId, sessionId, removed.getUsername());
        } else {
            log.warn("⚠️ {}_ No cache entry found for session: {}", sessionId, sessionId);
        }

        String queueName = userQueuePrefix + sessionId;
        try {
            rabbitMQAdminClient.deleteQueue(sessionId, queueName);
            log.debug("🗑️ {}_ Deleted RabbitMQ queue: {}", sessionId, queueName);
        } catch (Exception e) {
            log.error("❌ {}_ Error deleting queue: {}, error: {}", sessionId, queueName, e.getMessage(), e);
        }
    }
}
