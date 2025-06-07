package com.woow.security.api.ws;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.*;


@Data
public class WSCacheInput {
    private String sessionId;
    private String username;
    private Set<String> roles = new HashSet<>();
    private List<String> subscriptions = new ArrayList<>();
    private Map<String, String> subscriptionMap = new HashMap<>();
    private LocalDateTime connectedAt;
    private String ipAddress;
    private String userAgent;
    private Map<String, String> customMetadata = new HashMap<>();
}

