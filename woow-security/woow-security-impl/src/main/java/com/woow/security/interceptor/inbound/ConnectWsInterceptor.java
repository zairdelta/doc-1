package com.woow.security.interceptor.inbound;

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

@Component
@Slf4j
public class ConnectWsInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

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
