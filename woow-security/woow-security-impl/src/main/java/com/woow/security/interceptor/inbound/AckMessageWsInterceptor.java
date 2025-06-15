package com.woow.security.interceptor.inbound;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AckMessageWsInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (accessor.getCommand() == StompCommand.ACK) {
            String sessionId = accessor.getSessionId();
            String subscriptionId = accessor.getSubscriptionId();
            String messageId = accessor.getNativeHeader("id") != null
                    ? accessor.getNativeHeader("id").get(0)
                    : "unknown";

            log.info("✅ {}_ ACK received: sessionId={}, subscriptionId={}, " +
                    "messageId={}", sessionId, sessionId, subscriptionId, messageId);

        }

        return message;
    }
}
