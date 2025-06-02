package com.woow.security.interceptor.inbound;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class InboundMessageLoggingWsInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        String payloadConvertedToString;

        if (message.getPayload() == null) {
            payloadConvertedToString = "NO PAYLOAD";
        } else if (message.getPayload() instanceof byte[]) {
            payloadConvertedToString = new String((byte[]) message.getPayload(),
                    StandardCharsets.UTF_8);
        } else {
            payloadConvertedToString = message.getPayload().toString();
        }

        if (accessor.getCommand() != null) {
            log.info("Inbound STOMP message - Command: [{}]," +
                            " Destination: [{}], Session: [{}], Payload: {}, Receipt:{}",
                    accessor.getCommand(),
                    accessor.getDestination(),
                    accessor.getSessionId(),
                    payloadConvertedToString,
                    accessor.getReceipt());
        } else {
            log.warn("Inbound STOMP message command is null");
        }

        return message;
    }
}
