package com.woow.security.api.ws;

import org.springframework.context.ApplicationEvent;

public class StompSubscribeAppEvent extends ApplicationEvent {
    private final WSCacheInput wsCacheInput;
    private final String destination;

    public StompSubscribeAppEvent(Object source, WSCacheInput input, String destination) {
        super(source);
        this.wsCacheInput = input;
        this.destination = destination;
    }

    public WSCacheInput getWsCacheInput() {
        return wsCacheInput;
    }

    public String getDestination() {
        return destination;
    }
}