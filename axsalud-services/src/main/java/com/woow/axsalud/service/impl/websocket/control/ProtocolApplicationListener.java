package com.woow.axsalud.service.impl.websocket.control;

import com.woow.security.api.ws.StompConnectAppEvent;
import com.woow.security.api.ws.StompDisconnectAppEvent;
import com.woow.security.api.ws.StompSubscribeAppEvent;
import com.woow.security.api.ws.StompUnsubscribeAppEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProtocolApplicationListener {
    @EventListener
    public void onConnect(StompConnectAppEvent event) {
        log.info("✅ {}_ CONNECT: user={}", event.getWsCacheInput().getSessionId(),
                event.getWsCacheInput().getUsername());
    }

    @EventListener
    public void onDisconnect(StompDisconnectAppEvent event) {
        log.info("❌ {}_ DISCONNECT: user={}", event.getWsCacheInput().getSessionId(),
                event.getWsCacheInput().getUsername());
    }

    @EventListener
    public void onSubscribe(StompSubscribeAppEvent event) {
        log.info("➕ {}_ SUBSCRIBE: user={}, destination={}", event.getWsCacheInput().getSessionId(),
                event.getWsCacheInput().getUsername(), event.getDestination());
    }

    @EventListener
    public void onUnsubscribe(StompUnsubscribeAppEvent event) {
        log.info("➖ {}_ UNSUBSCRIBE: user={}, destination={}", event.getWsCacheInput().getSessionId(),
                event.getWsCacheInput().getUsername(), event.getDestination());
    }
}
