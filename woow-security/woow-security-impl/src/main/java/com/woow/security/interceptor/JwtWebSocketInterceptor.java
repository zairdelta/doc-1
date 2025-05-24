package com.woow.security.interceptor;

import com.woow.security.api.JwtTokenUtil;
import com.woow.security.api.JwtUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class JwtWebSocketInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

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
        }


        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            log.info("Getting STOMP CONNECT, getting authentication header");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwtToken = authHeader.substring(7);
                String username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                UserDetails userDetails = userDetailsService.loadUserByUsername(
                        username);


                if (username != null && jwtTokenUtil.validateToken(jwtToken, userDetails)) {

                    accessor.setUser(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()));
                }
            }
        } else if (accessor.getCommand() == StompCommand.SEND
                || accessor.getCommand() == StompCommand.MESSAGE) {
            log.info("INBOUND STOMP message to destination [{}], STOMP session [{}], payload: {}",
                    accessor.getDestination(),
                    accessor.getSessionId(),
                    message.getPayload());
        }

        return message;
    }
}
