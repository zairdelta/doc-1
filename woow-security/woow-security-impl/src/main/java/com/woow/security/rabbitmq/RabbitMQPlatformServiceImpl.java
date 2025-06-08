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
    public void appSessionTerminated(String appSessionId) {
        log.info("Application Session Terminated for appSessionId: {}", appSessionId);
       // rabbitMQAdminClient.deleteBindingThatContains(convertSessionIdToPlatformSessionId(appSessionId));
    }

    private String convertSessionIdToPlatformSessionId(final String appSessionId) {
        String bindingName = appSessionId.replaceFirst("^/topic/", "");
        return bindingName;
    }
}
