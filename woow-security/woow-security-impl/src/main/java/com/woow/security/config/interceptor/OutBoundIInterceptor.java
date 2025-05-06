package com.woow.security.config.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;


import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class OutBoundIInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (accessor.getCommand() == StompCommand.MESSAGE) {
            String payloadConvertedToString;
            if (message.getPayload() == null) {
                payloadConvertedToString = "NO PAYLOAD";
            } else if (message.getPayload() instanceof byte[]) {
                payloadConvertedToString = new String((byte[]) message.getPayload(),
                        StandardCharsets.UTF_8);
            } else {
                payloadConvertedToString = message.getPayload().toString();
            }

            log.info("SERVER OUTBOUND STOMP MESSAGE - Destination: [{}], " +
                            "Session: [{}], Payload: {}",
                    accessor.getDestination(),
                    accessor.getSessionId(),
                    payloadConvertedToString);
        }

        return message;
    }
}
