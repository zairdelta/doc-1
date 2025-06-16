package com.woow.it;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

public class RabbitMQClientTests {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQClientTests.class);
    private final RestTemplate restTemplate = new RestTemplate();

    private final String rabbitHost = "https://0rbjt9.stackhero-network.com";

     //private final String rabbitHost = "https://rqanay.stackhero-network.com";
    private final String username = "admin";
     private final String password = "9KLwnaP17gIdi8CoDetZIlHMgaaVxkqf";

    //private final String password = "ebDn8IZ6qtSn7waNHbV1lKS8cHdlZG6j";
    private final String VHOST = "/";

    public static void main(String[] args) {
        RabbitMQClientTests rabbitMQClientTests = new RabbitMQClientTests();
        rabbitMQClientTests.deleteIdleQueues(0);
    }

    public void deleteIdleQueues(int amountOfMessagesReady) {
        List<RabbitMQQueueInfo> queues = getAllQueues();

        for (RabbitMQQueueInfo queue : queues) {
            if (queue.getMessagesReady() >= amountOfMessagesReady &&
                    queue.getMessagesUnacknowledged() == 0 &&
                    queue.getConsumers() == 0) {
                log.info("Queue with 0 consumers and more than: {} messages waiting. QueueInfo: {}", amountOfMessagesReady, queue);
                deleteQueue(queue.getName());
            }
        }
    }


    public void deleteQueueFrom(final String bindingName) {
        try {
            RabbitMQClientTests rabbitMQClientTests = new RabbitMQClientTests();
            List<RabbitMQBinding> bindings = rabbitMQClientTests.getAllBindings();
            log.info("🔗 Total bindings: {}", bindings.size());

            for (RabbitMQBinding binding : bindings) {
                if (binding.getRoutingKey().equals(bindingName) &&
                        "queue".equalsIgnoreCase(binding.getDestinationType())) {
                    String queueName = binding.getDestination();
                    log.info("🧹 Deleting queue '{}' matching binding '{}'", queueName, bindingName);
                    deleteQueue(queueName);
                    return;
                }
            }
        } catch (Exception e) {
            log.error("❌ Error fetching bindings: {}", e.getMessage(), e);
        }
    }

    public List<RabbitMQQueueInfo> getAllQueues() {
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


    private List<RabbitMQBinding> getAllBindings() {
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

    public void deleteQueue(String queueName) {
        String urlString = rabbitHost + "/api/queues/%2F/" + queueName;
        URI uri = UriComponentsBuilder
                .fromHttpUrl(urlString)
                .build(true)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.DELETE, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("✅ Successfully deleted queue: {}", queueName);
        } else {
            log.warn("⚠️ Failed to delete queue: {} — Status: {}", queueName, response.getStatusCode());
        }
    }

}
