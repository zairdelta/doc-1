package com.woow.security;

import com.woow.security.api.JwtTokenUtil;
import com.woow.security.api.WebSocketUserPrincipal;
import com.woow.security.rabbitmq.RabbitMQStompBrokerProperties;
import com.woow.security.interceptor.JwtWebSocketInterceptor;
import com.woow.security.interceptor.OutBoundIInterceptor;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompReactorNettyCodec;
import org.springframework.messaging.tcp.reactor.ReactorNettyTcpClient;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpClient;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private JwtTokenUtil jwtTokenUtil;
    private final JwtWebSocketInterceptor jwtWebSocketInterceptor;
    private final OutBoundIInterceptor outBoundIInterceptor;
    private RabbitMQStompBrokerProperties rabbitMQStompBrokerProperties;

    public WebSocketConfig(JwtWebSocketInterceptor jwtWebSocketInterceptor,
                           OutBoundIInterceptor outBoundIInterceptor,
                           JwtTokenUtil jwtTokenUtil,
                           RabbitMQStompBrokerProperties rabbitMQStompBrokerProperties) {
        this.jwtWebSocketInterceptor = jwtWebSocketInterceptor;
        this.outBoundIInterceptor = outBoundIInterceptor;
        this.jwtTokenUtil = jwtTokenUtil;
        this.rabbitMQStompBrokerProperties = rabbitMQStompBrokerProperties;
    }


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request,
                                                      WebSocketHandler wsHandler,
                                                      Map<String, Object> attributes) {

                        log.info("DetermineUser getting request");
                        HttpHeaders headers = request.getHeaders();
                        headers.forEach((key, value) -> {
                            log.info("Header '{}' = {}", key, value);
                        });

                        if (request.getHeaders().get("Authorization") == null) {
                            log.info("Authorization header is not present in web socket call");
                            return null;

                        } else {
                            String authHeader = request.getHeaders().get("Authorization").get(0);
                            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                                String jwtToken = authHeader.substring(7);
                                String username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                                List<String> roles = jwtTokenUtil.getRoles(jwtToken);
                                log.info("UserName ConnectedTo Socket from token: {}", username);
                                return new WebSocketUserPrincipal(username, roles);
                            }

                            return null;

                        }}})
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Bean
    public ReactorNettyTcpClient<byte[]> stompTcpClient() {
        SslContext sslContext;
        try {
            sslContext = SslContextBuilder
                    .forClient()
                    .build();
        } catch (SSLException e) {
            throw new IllegalStateException("Failed to create SSL context", e);
        }

        ConnectionProvider connectionProvider =
                ConnectionProvider.builder(rabbitMQStompBrokerProperties.getConnectionPoolName())
                        .maxIdleTime(Duration.ofSeconds(60000000)) // 10 minutes
                        .maxLifeTime(Duration.ofSeconds(60000000)) // 10 minutes
                .maxConnections(rabbitMQStompBrokerProperties.getMaxConnections())
                .pendingAcquireMaxCount(rabbitMQStompBrokerProperties.getPendingAcquireMaxCount())
                .pendingAcquireTimeout(Duration.ofSeconds(rabbitMQStompBrokerProperties.getPendingAcquireTimeoutInSeconds()))
                .build();

        TcpClient sslClient = TcpClient.create(connectionProvider)
                .secure(ssl -> ssl.sslContext(sslContext))
                .option(ChannelOption.SO_KEEPALIVE, true)
              //  .doOnConnected(conn ->
               //         conn.addHandlerLast(new ReadTimeoutHandler(60))
                //                .addHandlerLast(new WriteTimeoutHandler(60))
               // )
                .remoteAddress(() ->
                        new InetSocketAddress(rabbitMQStompBrokerProperties.getRelayHost(),
                                rabbitMQStompBrokerProperties.getRelayPort()));

        return new ReactorNettyTcpClient<>(client -> sslClient, new StompReactorNettyCodec());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        config.enableStompBrokerRelay("/topic", "/queue")
                .setSystemHeartbeatSendInterval(5000)
                .setSystemHeartbeatReceiveInterval(12000)
                .setRelayHost(rabbitMQStompBrokerProperties.getRelayHost())
                .setRelayPort(rabbitMQStompBrokerProperties.getRelayPort())
                .setClientLogin(rabbitMQStompBrokerProperties.getClientLogin())
                .setClientPasscode(rabbitMQStompBrokerProperties.getClientPasscode())
                .setSystemLogin(rabbitMQStompBrokerProperties.getSystemLogin())
                .setSystemPasscode(rabbitMQStompBrokerProperties.getSystemPasscode())
                .setTcpClient(stompTcpClient());

        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user/");
    }

    /*@Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/queue", "/topic");
    }
     */

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtWebSocketInterceptor);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(outBoundIInterceptor);
    }

}
