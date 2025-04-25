package com.woow.it;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.ArrayList;
import java.util.List;

public class MySockJSClient {

    public static void mains(String[] args) throws Exception {
        // WebSocket client
        StandardWebSocketClient standardWebSocketClient = new StandardWebSocketClient();

        // WebSocket HTTP headers
        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        handshakeHeaders.add("Authorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6WyJBRE1JTiJdLCJ1c2VySWQiOjEsInN1YiI6Im1hc3RlckBleGFtcGxlLmNvbSIsImlhdCI6MTc0NTU5MjYxMSwiZXhwIjoxNzQ1NjEwNjExfQ.UFSl7O2hcNuYg6l1T-SvpUNsa4YPECWRubglSt2v9FKQwv5PCh5ZH8X9VIMd83o4_dKQpno7ShiM73T67EjQ4Q");

        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(standardWebSocketClient));
        SockJsClient sockJsClient = new SockJsClient(transports);

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());


        String url = "https://telemedicine-backend-dev-652309c07146.herokuapp.com/ws";
        StompHeaders stompHeaders = new StompHeaders();

        StompSession session = stompClient.connectAsync(url,
                handshakeHeaders,
                stompHeaders,
                new StompSessionHandlerAdapter() {}
        ).get();

        System.out.println("Connected!");

        Thread.sleep(60000);
    }
}
