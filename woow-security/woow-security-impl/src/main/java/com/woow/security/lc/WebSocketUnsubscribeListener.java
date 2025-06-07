package com.woow.security.lc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Component
@Slf4j
public class WebSocketUnsubscribeListener {

    @Autowired
    private WSCache wsCache;

    @EventListener
    public void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String subscriptionId = accessor.getSubscriptionId(); // 🔑 this is all we get

        log.debug("📤 STOMP UNSUBSCRIBE: sessionId={}, subscriptionId={}", sessionId, subscriptionId);

        if (subscriptionId != null && sessionId != null) {
            wsCache.removeSubscription(sessionId, subscriptionId);
        }
    }
}
