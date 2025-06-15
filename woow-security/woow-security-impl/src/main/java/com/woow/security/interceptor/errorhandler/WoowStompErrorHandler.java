package com.woow.security.interceptor.errorhandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class WoowStompErrorHandler extends StompSubProtocolErrorHandler {

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {

        StompHeaderAccessor originalAccessor = StompHeaderAccessor.wrap(clientMessage);
        String sessionId = originalAccessor.getSessionId();

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setLeaveMutable(true);
        accessor.setSessionId(sessionId);
        accessor.setSubscriptionId(originalAccessor.getSubscriptionId());
        accessor.setReceiptId(originalAccessor.getReceiptId());
        accessor.setDestination(originalAccessor.getDestination());
        accessor.setUser(originalAccessor.getUser());

        String errorMessage = "Custom STOMP error: " + ex.getMessage();
        accessor.setMessage(errorMessage);

        log.error("{}_ STOMP error - Command: {}, SubscriptionId: {}," +
                        " Destination: {}, User: {}, Headers: {}, Payload: {}, Exception: {}",
                sessionId,
                originalAccessor.getCommand(),
                originalAccessor.getSubscriptionId(),
                originalAccessor.getDestination(),
                originalAccessor.getUser(),
                originalAccessor.toNativeHeaderMap(),
                new String(clientMessage.getPayload(), StandardCharsets.UTF_8),
                ex.toString()
        );

        return MessageBuilder
                .createMessage(errorMessage.getBytes(StandardCharsets.UTF_8), accessor.getMessageHeaders());
    }

    @Override
    public Message<byte[]> handleErrorMessageToClient(Message<byte[]> errorMessage) {
      // code could transform error sent by broker
        String rawError = new String(errorMessage.getPayload());
        log.error("Received STOMP ERROR from broker: " + rawError);

        return super.handleErrorMessageToClient(errorMessage);
    }
}