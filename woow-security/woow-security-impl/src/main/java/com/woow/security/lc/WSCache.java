package com.woow.security.lc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WSCache {

    private final Map<String, WSCacheInput> sessionCache = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> destinationToSessions = new ConcurrentHashMap<>();

    public void addOrUpdateSession(String sessionId, WSCacheInput input) {
        sessionCache.put(sessionId, input);
        log.debug("✅ Session added/updated: {}", input);
    }

    public void addSubscription(String sessionId, String subscriptionId, String destination) {
        WSCacheInput input = sessionCache.get(sessionId);
        if (input != null) {
            input.getSubscriptions().add(destination);
            input.getSubscriptionMap().put(subscriptionId, destination);
        }
    }

    public void removeSubscription(String sessionId, String subscriptionId) {
        WSCacheInput input = sessionCache.get(sessionId);
        if (input != null) {
            String destination = input.getSubscriptionMap().remove(subscriptionId);
            input.getSubscriptions().remove(destination);
        }
    }


    public void addCustomMetadata(String sessionId, String key, String value) {
        WSCacheInput input = sessionCache.get(sessionId);
        if (input != null) {
            input.getCustomMetadata().put(key, value);
            log.debug("🔧 Metadata added: session={}, key={}, value={}", sessionId, key, value);
        }
    }

    public WSCacheInput getSession(String sessionId) {
        return sessionCache.get(sessionId);
    }

    public Set<String> getSessionsSubscribedTo(String destination) {
        return destinationToSessions.getOrDefault(destination, Collections.emptySet());
    }

    public void removeSession(String sessionId) {
        WSCacheInput removed = sessionCache.remove(sessionId);
        if (removed != null) {
            for (String destination : removed.getSubscriptions()) {
                removeSubscription(sessionId, destination);
            }
            log.debug("❌ Session removed: sessionId={}, data={}", sessionId, removed);
        }
    }

    public Map<String, WSCacheInput> getAllSessions() {
        return sessionCache;
    }
}
