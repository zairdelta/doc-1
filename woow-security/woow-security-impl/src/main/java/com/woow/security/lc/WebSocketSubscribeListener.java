package com.woow.security.lc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
@Slf4j
public class WebSocketSubscribeListener {

    @Autowired
    private WSCache wsCache;

    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();
        String subscriptionId = accessor.getSubscriptionId(); // ✅ Important

        log.debug("📥 STOMP SUBSCRIBE: sessionId={}, subscriptionId={}, destination={}",
                sessionId, subscriptionId, destination);

        if (destination != null && subscriptionId != null) {
            wsCache.addSubscription(sessionId, subscriptionId, destination);
        }
    }
}
