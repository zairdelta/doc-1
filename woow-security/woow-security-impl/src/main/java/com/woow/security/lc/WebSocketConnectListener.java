package com.woow.security.lc;

import com.woow.security.api.ws.StompConnectAppEvent;
import com.woow.security.api.ws.WSCacheInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class WebSocketConnectListener {

    @Autowired
    private WSCache wsCache;

    @Autowired
    private ApplicationEventPublisher publisher;

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = event.getUser();
        String sessionId = accessor.getSessionId();

        log.info("✅ {}_ , STOMP session connected: {}", sessionId, sessionId);

        WSCacheInput input = createWSCacheInput(event, accessor, principal);
        wsCache.addOrUpdateSession(sessionId, input);
        publisher.publishEvent(new StompConnectAppEvent(this, input));
    }

    private WSCacheInput createWSCacheInput(SessionConnectedEvent event, StompHeaderAccessor accessor, Principal principal) {
        WSCacheInput input = new WSCacheInput();
        input.setSessionId(accessor.getSessionId());
        input.setConnectedAt(LocalDateTime.now());

        // User info
        if (principal != null) {
            input.setUsername(principal.getName());

            // Extract roles if Principal is a Spring Security Authentication
            if (principal instanceof org.springframework.security.core.Authentication authentication) {
                List<String> roles = authentication.getAuthorities().stream()
                        .map(a -> a.getAuthority())
                        .collect(Collectors.toList());
                input.getRoles().addAll(roles);
            }
        }

        Object ip = accessor.getSessionAttributes() != null
                ? accessor.getSessionAttributes().get("ip")
                : null;
        input.setIpAddress(ip != null ? ip.toString() : "unknown");

        String userAgent = accessor.getFirstNativeHeader("User-Agent");
        input.setUserAgent(userAgent != null ? userAgent : "unknown");

        return input;
    }
}
