package com.woow.axsalud.service.impl.websocket.control;

import com.woow.security.api.ws.WSQueueNamesHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Slf4j
@Component
public class WSQueueNameHandlerImpl implements WSQueueNamesHandler {
    private static final String QUEUE_PREFIX = "-queue";
    @Override
    public String parseQueueNameFrom(String sessionId, String subscriptioinId) {
        String queueName = "";

        log.info("{}_ getting queueName from subscriptionId: {}", sessionId, subscriptioinId);

        if(ObjectUtils.isEmpty(subscriptioinId)) {
            log.info("{}_ subscriptionId is empty or null {}", sessionId, subscriptioinId);
        } else
        if(subscriptioinId.contains("doctor-events")) {
            queueName = subscriptioinId + QUEUE_PREFIX;
            log.info("{}_ getting queueName from subscriptionId: {} contains doctor events, result: {}",
                    sessionId, subscriptioinId, queueName);
        } else if(subscriptioinId.contains("control")) {
            queueName = subscriptioinId + QUEUE_PREFIX;
            log.info("{}_ getting queueName from subscriptionId: {} contains control Session, result: {}",
                    sessionId, subscriptioinId, queueName);
        }
        return queueName;
    }
}
