package com.woow.security.api.ws;

import org.springframework.context.ApplicationEvent;

public class StompConnectAppEvent extends ApplicationEvent {
    private final WSCacheInput wsCacheInput;

    public StompConnectAppEvent(Object source, WSCacheInput input) {
        super(source);
        this.wsCacheInput = input;
    }

    public WSCacheInput getWsCacheInput() {
        return wsCacheInput;
    }
}

