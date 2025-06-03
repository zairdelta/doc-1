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
public class SubscribeWsInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
         log.info("validating if SUBSCRIBE command was received");
        if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            accessor.setNativeHeader("durable", "true");
            accessor.setNativeHeader("auto-delete", "false");
          //  accessor.setNativeHeader("x-expires", "120000");
            log.info("Injected headers for subscription: " + accessor.toNativeHeaderMap());
        } else {
            log.info("SUBSCRIBE command was not received, accessor: {}", accessor);
        }

        return message;
    }
}
