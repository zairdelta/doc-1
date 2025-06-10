package com.woow.axsalud.service.impl.websocket.control;

import com.woow.axsalud.common.UserStatesEnum;
import com.woow.axsalud.data.repository.AxSaludUserRepository;
import com.woow.axsalud.service.api.ConsultationService;
import com.woow.security.api.ws.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

        Optional<String> controlSessionSubscriptionOptional =
                getControlSubscriptions(event.getWsCacheInput().getSubscriptions());

        if(controlSessionSubscriptionOptional.isEmpty()) {
            log.warn("❌ {}_ control session not present in subscriptions, userName: {}, event: {}", event.getWsCacheInput().getSessionId(),
                    event.getWsCacheInput().getUsername(), event.getWsCacheInput());
        } else {
            handledConsultationDisconnect(controlSessionSubscriptionOptional, event.getWsCacheInput());
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

        Optional<String> controlSessionSubscriptionOptional =
                getControlSubscriptions(event.getWsCacheInput().getSubscriptions());

        if(controlSessionSubscriptionOptional.isEmpty()) {
            log.warn("❌ {}_ control session not present in subscriptions, userName: {}, event: {}",
                    event.getWsCacheInput().getSessionId(),
                    event.getWsCacheInput().getUsername(), event.getWsCacheInput());
        } else {
            handledConsultationDisconnect(controlSessionSubscriptionOptional, event.getWsCacheInput());
        }

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

    private Optional<String> getControlSubscriptions(List<String> subscriptions) {
        //TODO to support multiple subscription we might need to find all that has control
        Optional<String> controlSessionSubscriptionOptional =
                subscriptions
                .stream()
                .filter(s -> PATTERN_CONTROL_SESSION.matcher(s).matches())
                .findFirst();
        return controlSessionSubscriptionOptional;
    }

    private void handledConsultationDisconnect(Optional<String> controlSessionSubscriptionOptional,
                                               WSCacheInput cacheInput) {

        if(controlSessionSubscriptionOptional.isEmpty()) {
            log.info("{}_ no control subscriptions received: {}", cacheInput.getSessionId(),
                    cacheInput);
        } else {
            ConsultationSessionIdVO consultationSessionIdVO =
                    getConsultationIds(controlSessionSubscriptionOptional.get());
            log.info("{}_ consultationSessionId DISCONNECT received: {}", cacheInput.getSessionId(),
                    consultationSessionIdVO);
            consultationService.consultationDisconnect(cacheInput.getSessionId(),
                    consultationSessionIdVO.getConsultationId(), consultationSessionIdVO.getConsultationSessionId(),
                    cacheInput.getUsername(),
                    cacheInput.getRoles().stream().findFirst().orElse(""));
        }
    }

}
