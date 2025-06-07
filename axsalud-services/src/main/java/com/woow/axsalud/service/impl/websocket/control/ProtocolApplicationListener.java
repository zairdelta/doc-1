package com.woow.axsalud.service.impl.websocket.control;

import com.woow.axsalud.data.repository.AxSaludUserRepository;
import com.woow.security.api.ws.StompConnectAppEvent;
import com.woow.security.api.ws.StompDisconnectAppEvent;
import com.woow.security.api.ws.StompSubscribeAppEvent;
import com.woow.security.api.ws.StompUnsubscribeAppEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ProtocolApplicationListener {

    private AxSaludUserRepository axSaludUserRepository;

    public ProtocolApplicationListener(final AxSaludUserRepository axSaludUserRepository) {
        this.axSaludUserRepository = axSaludUserRepository;
    }
    @EventListener
    public void onConnect(StompConnectAppEvent event) {
        log.info("✅ {}_ CONNECT: user={}", event.getWsCacheInput().getSessionId(),
                event.getWsCacheInput().getUsername());
    }

    @EventListener
    public void onDisconnect(StompDisconnectAppEvent event) {
        log.info("❌ {}_ DISCONNECT: user={}", event.getWsCacheInput().getSessionId(),
                event.getWsCacheInput().getUsername());
    }

    @EventListener
    public void onSubscribe(StompSubscribeAppEvent event) {
        log.info("➕ {}_ SUBSCRIBE: user={}, destination={}, subscriptionId={}",
                event.getWsCacheInput().getSessionId(),
                event.getWsCacheInput().getUsername(),
                event.getDestination(), event.getWsCacheInput().getSessionId());
    }

    @EventListener
    public void onUnsubscribe(StompUnsubscribeAppEvent event) {
        log.info("➖ {}_ UNSUBSCRIBE: user={}, destination={}", event.getWsCacheInput().getSessionId(),
                event.getWsCacheInput().getUsername(), event.getDestination());
    }

    private static ConsultationSessionIdVO parseTopic(String topic) {
        ConsultationSessionIdVO
                consultationSessionIdVO = new ConsultationSessionIdVO();
        String pattern = "/topic/consultation\\.([^.]+)\\.session\\.([^.]+)\\.control";

        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(topic);

        if (matcher.matches()) {
            consultationSessionIdVO.setConsultationId(matcher.group(1));
            consultationSessionIdVO.setConsultationSessionId(matcher.group(2));
        }

        return consultationSessionIdVO;
    }
}
