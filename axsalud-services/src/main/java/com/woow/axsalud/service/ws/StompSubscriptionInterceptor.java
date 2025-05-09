package com.woow.axsalud.service.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@Slf4j
public class StompSubscriptionInterceptor implements ChannelInterceptor {

    public StompSubscriptionInterceptor() {
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (StompCommand.SUBSCRIBE.equals(command)) {
            String destination = accessor.getDestination();
            Principal user = accessor.getUser();
            String subscriptionId = accessor.getSubscriptionId();
            String sessionId = accessor.getSessionId();


        }

        if (StompCommand.UNSUBSCRIBE.equals(command)) {
            String subscriptionId = accessor.getSubscriptionId();
            String sessionId = accessor.getSessionId();
            Principal user = accessor.getUser();


        }
        return message;
    }
}
