package com.woow.security.lc;

import com.woow.security.api.ws.StompUnsubscribeAppEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Component
@Slf4j
public class WebSocketUnsubscribeListener {

    @Autowired
    private WSCache wsCache;

    @Autowired
    private ApplicationEventPublisher publisher;
    @EventListener
    public void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String subscriptionId = accessor.getSubscriptionId();
        String destination = accessor.getDestination();

        log.debug("📤 {}_ STOMP UNSUBSCRIBE: sessionId={}, subscriptionId={}", sessionId, sessionId, subscriptionId);

        if (subscriptionId != null && sessionId != null) {
            wsCache.removeSubscription(sessionId, subscriptionId);
            publisher.publishEvent(new StompUnsubscribeAppEvent(this,
                    wsCache.getSession(sessionId), destination));
        }
    }
}
