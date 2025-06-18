package com.woow.security.interceptor.inbound;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StompLoggingWsInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            StompCommand command = accessor.getCommand();
            String sessionId = accessor.getSessionId() != null ? accessor.getSessionId() : "unknown-session";

            if (command != null) {
                log.debug("🔍 {}_ STOMP [{}] headers:", sessionId, command);
                accessor.toNativeHeaderMap().forEach((key, value) -> {
                    log.debug("📌 {}_ {}: {}", sessionId, key, value);
                });

                if (StompCommand.ERROR.equals(command)) {
                    Object rawPayload = message.getPayload();
                    String payloadStr = rawPayload instanceof byte[]
                            ? new String((byte[]) rawPayload)
                            : String.valueOf(rawPayload);
                    log.error("❗ {}_ STOMP ERROR payload: {}", sessionId, payloadStr);
                }
            }
        }

        return message;
    }
}