package com.woow.security.api.ws;

import org.springframework.context.ApplicationEvent;

public class StompUnsubscribeAppEvent extends ApplicationEvent {
    private final WSCacheInput wsCacheInput;
    private final String destination;
    private final String subscriptionId;

    public StompUnsubscribeAppEvent(Object source, WSCacheInput input, String destination, String subscriptionId) {
        super(source);
        this.wsCacheInput = input;
        this.destination = destination;
        this.subscriptionId = subscriptionId;
    }

    public WSCacheInput getWsCacheInput() {
        return wsCacheInput;
    }

    public String getDestination() {
        return destination;
    }

    public String getSubscriptionId() {return subscriptionId;}
}