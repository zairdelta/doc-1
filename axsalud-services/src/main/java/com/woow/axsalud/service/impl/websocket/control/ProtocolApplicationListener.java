package com.woow.axsalud.service.impl.websocket.control;

import com.woow.axsalud.common.UserStatesEnum;
import com.woow.axsalud.data.repository.AxSaludUserRepository;
import com.woow.axsalud.service.api.ConsultationService;
import com.woow.security.api.ws.StompConnectAppEvent;
import com.woow.security.api.ws.StompDisconnectAppEvent;
import com.woow.security.api.ws.StompSubscribeAppEvent;
import com.woow.security.api.ws.StompUnsubscribeAppEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ProtocolApplicationListener {

    private AxSaludUserRepository axSaludUserRepository;
    private ConsultationService consultationService;

    private final static String CONSULTATION_CONTROL_PATTERN = "/topic/consultation\\.([^.]+)\\.session\\.([^.]+)\\.control";
    private final static Pattern PATTERN_CONTROL_SESSION = Pattern.compile(CONSULTATION_CONTROL_PATTERN);
    public ProtocolApplicationListener(final AxSaludUserRepository axSaludUserRepository,
                                       final ConsultationService consultationService) {
        this.axSaludUserRepository = axSaludUserRepository;
        this.consultationService = consultationService;
    }
    @EventListener
    @Transactional
    public void onConnect(StompConnectAppEvent event) {
        log.info("✅ {}_ CONNECT: user={}, making user online", event.getWsCacheInput().getSessionId(),
                event.getWsCacheInput().getUsername());
        axSaludUserRepository.updateUserStateByCoreUserId(event.getWsCacheInput().getUsername(),
                UserStatesEnum.ONLINE);
    }

    @EventListener
    @Transactional
    public void onDisconnect(StompDisconnectAppEvent event) {
        log.info("❌ {}_ DISCONNECT: user={} making user OFFLINE", event.getWsCacheInput().getSessionId(),
                event.getWsCacheInput().getUsername());
        axSaludUserRepository.updateUserStateByCoreUserId(event.getWsCacheInput().getUsername(),
                UserStatesEnum.OFFLINE);

        Optional<String> controlSessionSubscriptionOptional = event.getWsCacheInput()
                .getSubscriptions()
                .stream()
                .filter(s -> PATTERN_CONTROL_SESSION.matcher(s).matches())
                .findFirst();

        if(controlSessionSubscriptionOptional.isEmpty()) {
            log.warn("❌ {}_ control session not present in subscriptions, userName: {}, event: {}", event.getWsCacheInput().getSessionId(),
                    event.getWsCacheInput().getUsername(), event.getWsCacheInput());
        } else {
            ConsultationSessionIdVO consultationSessionIdVO =
                    getConsultationIds(controlSessionSubscriptionOptional.get());
            log.info("{}_ consultationSessionId DISCONNECT received: {}", event.getWsCacheInput().getSessionId(),
                    consultationSessionIdVO);
            consultationService.consultationDisconnect(event.getWsCacheInput().getSessionId(),
                    consultationSessionIdVO.getConsultationId(), consultationSessionIdVO.getConsultationSessionId(),
                    event.getWsCacheInput().getUsername(),
                    event.getWsCacheInput().getRoles().stream().findFirst().orElse(""));
        }
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
        log.info("➖ {}_ UNSUBSCRIBE: user={}, destination={}, subscriptionID=: {}",
                event.getWsCacheInput().getSessionId(),
                event.getWsCacheInput().getUsername(), event.getDestination(), event.getSubscriptionId());
    }

    private static ConsultationSessionIdVO getConsultationIds(String subscription) {
        ConsultationSessionIdVO
                consultationSessionIdVO = new ConsultationSessionIdVO();


        Pattern regex = Pattern.compile(CONSULTATION_CONTROL_PATTERN);
        Matcher matcher = regex.matcher(subscription);

        if (matcher.matches()) {
            consultationSessionIdVO.setConsultationId(matcher.group(1));
            consultationSessionIdVO.setConsultationSessionId(matcher.group(2));
        }

        return consultationSessionIdVO;
    }
}
