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
public class SendMessageWsInterceptor   implements ChannelInterceptor {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

            if (accessor != null && (accessor.getCommand() == StompCommand.SEND
                    || accessor.getCommand() == StompCommand.MESSAGE)) {
                accessor.setNativeHeader("durable", "true");
                accessor.setNativeHeader("auto-delete", "false");
                accessor.setNativeHeader("x-expires", "120000");
                log.info("Injected headers for SEND MESSAGE: " + accessor.toNativeHeaderMap());
                log.info("INBOUND STOMP message to destination [{}], STOMP session [{}], payload: {}",
                        accessor.getDestination(),
                        accessor.getSessionId(),
                        message.getPayload());
            }

            return message;
        }
}
