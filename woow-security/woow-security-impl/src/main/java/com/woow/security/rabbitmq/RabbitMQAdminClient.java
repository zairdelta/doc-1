package com.woow.security.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.woow.security.api.ws.StompDisconnectAppEvent;
import com.woow.security.api.ws.StompUnsubscribeAppEvent;
import com.woow.security.api.ws.WSQueueNamesHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.util.List;

@Component
@Slf4j
public class RabbitMQAdminClient {

    private final RabbitMQStompBrokerProperties stompBrokerProperties;

    private WSQueueNamesHandler wsQueueNamesHandler;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String rabbitHost;
    private final String username;
    private final String password;
    private final String VHOST = "/";

    public RabbitMQAdminClient(WSQueueNamesHandler wsQueueNamesHandler, RabbitMQStompBrokerProperties stompBrokerProperties) {
        this.stompBrokerProperties = stompBrokerProperties;
        this.wsQueueNamesHandler = wsQueueNamesHandler;
        this.rabbitHost = "https://" + stompBrokerProperties.getRelayHost();
        this.username = stompBrokerProperties.getSystemLogin();
        this.password = stompBrokerProperties.getSystemPasscode();
    }

    public void deleteQueue(String sessionId, String queueName) throws Exception {
        log.debug("{}_ Deleting queue from RabbitMQ: {}", sessionId, queueName);
        TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;


        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(stompBrokerProperties.getRelayHost());
        factory.setPort(stompBrokerProperties.getAdminPort());
        factory.setUsername(stompBrokerProperties.getClientLogin());
        factory.setPassword(stompBrokerProperties.getClientPasscode());
        factory.useSslProtocol(sslContext);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDelete(queueName);
            log.debug("{}_ Delete queue command send to: {}", sessionId, stompBrokerProperties.getRelayHost());
        }
    }

    public void deleteIdleQueues ( int amountOfMessagesReady) {
        List<RabbitMQQueueInfo> queues = getAllQueues();

        for (RabbitMQQueueInfo queue : queues) {
            if (queue.getMessagesReady() >= amountOfMessagesReady &&
                    queue.getMessagesUnacknowledged() == 0 &&
                    queue.getConsumers() == 0) {
                log.info("Queue with 0 consumers and more than: {} messages waiting. QueueInfo: {}", amountOfMessagesReady, queue);
                deleteQueueUsingRestClient(queue.getName());
            }
        }

    }

    public void deleteBindingThatContains (final String bindingName){
        try {
            log.info("Deleting queue that contains binding name: {}", bindingName);

            List<RabbitMQBinding> bindings = getAllBindings();
            log.info("🔗 Total bindings: {}", bindings.size());

            for (RabbitMQBinding binding : bindings) {
                if (binding.getRoutingKey().contains(bindingName) &&
                        "queue".equalsIgnoreCase(binding.getDestinationType())) {
                    String queueName = binding.getDestination();
                    log.info("🧹 Deleting queue '{}' matching binding '{}'", queueName, bindingName);
                    deleteQueueUsingRestClient(queueName);
                    return;
                }
            }
        } catch (Exception e) {
            log.error("❌ Error fetching bindings: {}", e.getMessage(), e);
        }
    }

    public List<RabbitMQQueueInfo> getAllQueues () {
        String url = rabbitHost + "/api/queues/%2F";

        URI uri = UriComponentsBuilder
                .fromHttpUrl(url)
                .build(true)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<RabbitMQQueueInfo[]> response = restTemplate.exchange(uri,
                HttpMethod.GET, entity, RabbitMQQueueInfo[].class);

        if (response.getBody() == null) throw new RuntimeException("No queues found");
        return List.of(response.getBody());
    }


    private List<RabbitMQBinding> getAllBindings () {
        String url = rabbitHost + "/api/bindings";
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<RabbitMQBinding[]> response = restTemplate.exchange(url, HttpMethod.GET,
                entity, RabbitMQBinding[].class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Failed to fetch bindings");
        }

        return List.of(response.getBody());
    }

    public void deleteQueueUsingRestClient (String queueName) {
        String urlString = rabbitHost + "/api/queues/%2F/" + queueName;
        URI uri = UriComponentsBuilder
                .fromHttpUrl(urlString)
                .build(true)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(uri, HttpMethod.DELETE, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("✅ Successfully deleted queue: {}", queueName);
        } else {
            log.warn("⚠️ Failed to delete queue: {} — Status: {}", queueName, response.getStatusCode());
        }
    }

    @EventListener
    public void onUnsubscribe(StompUnsubscribeAppEvent event) {
        log.info("➖ {}_ UNSUBSCRIBE Removing queue for: user={}, destination={}, subscriptionID:{}", event.getWsCacheInput().getSessionId(),
                event.getWsCacheInput().getUsername(), event.getDestination(), event.getSubscriptionId());
        try {
            String queueName = wsQueueNamesHandler.parseQueueNameFrom(event.getWsCacheInput().getSessionId(),
                    event.getSubscriptionId());
            if(ObjectUtils.isEmpty(queueName)) {
                log.info("➖ {}_ UNSUBSCRIBE Removing queue cannot be done for: user={}, destination={}, subscriptionID:{}, " +
                                "queueName is empty", event.getWsCacheInput().getSessionId(),
                        event.getWsCacheInput().getUsername(), event.getDestination(), event.getSubscriptionId());
            } else {
                this.deleteQueue(event.getWsCacheInput().getSessionId(), queueName);
            }
        } catch (Exception e) {
            log.error("Error deleting queName: {}", event.getSubscriptionId() + "-queue");
        }

    }

    @EventListener
    public void onDisconnect(StompDisconnectAppEvent event) {

    }

}

