package com.woow.security.rabbitmq;

import com.woow.security.api.ws.PlatformService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RabbitMQPlatformServiceImpl implements PlatformService {

    private RabbitMQAdminClient rabbitMQAdminClient;

    public RabbitMQPlatformServiceImpl(final RabbitMQAdminClient rabbitMQAdminClient) {
        this.rabbitMQAdminClient = rabbitMQAdminClient;
    }
    @Override
    public void appSessionTerminated(String sessionId) {
        log.info("Application Session Terminated for sessionId: {}", sessionId);
        rabbitMQAdminClient.deleteBindingThatContains(convertSessionIdToPlatformSessionId(sessionId));
    }

    private String convertSessionIdToPlatformSessionId(final String sessionId) {
        String bindingName = sessionId.replace("/", "_");
        bindingName = sessionId.replace(".", "_");
        return bindingName;
    }
}
