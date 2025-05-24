package com.woow.axsalud.service.impl.websocket;

import com.woow.axsalud.service.api.websocket.WebSocketObservabilityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WebSocketObservabilityServiceImpl implements WebSocketObservabilityService {

    private SimpUserRegistry simpUserRegistry;

    public WebSocketObservabilityServiceImpl(SimpUserRegistry simpUserRegistry) {
        this.simpUserRegistry = simpUserRegistry;

    }
    @Override
    public void logConnectedSessions() {
        for (SimpUser user : simpUserRegistry.getUsers()) {
            log.debug("User: {}", user.getName());
            for (SimpSession session : user.getSessions()) {
                log.debug(" {} - {} ", user.getName(), session.getId());
            }
        }
    }
}
