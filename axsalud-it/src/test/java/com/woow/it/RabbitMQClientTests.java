package com.woow.it;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Scanner;

public class RabbitMQClientTests {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQClientTests.class);
    private final RestTemplate restTemplate = new RestTemplate();

    private final String username = "admin";
    private String rabbitHost;
    private String password;
    private final String VHOST = "/";

    public RabbitMQClientTests(String environment) {
        switch (environment.toLowerCase()) {
            case "prod":
                this.rabbitHost = "https://0rbjt9.stackhero-network.com";
                this.password = "9KLwnaP17gIdi8CoDetZIlHMgaaVxkqf";
                break;
            case "dev":
                this.rabbitHost = "https://rqanay.stackhero-network.com";
                this.password = "ebDn8IZ6qtSn7waNHbV1lKS8cHdlZG6j";
                break;
            default:
                throw new IllegalArgumentException("Unknown environment: " + environment);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter environment (dev/prod): ");
        String env = scanner.nextLine().trim().toLowerCase();

        if (!env.equals("dev") && !env.equals("prod")) {
            System.err.println("❌ Invalid input. Please type 'dev' or 'prod'.");
            System.exit(1);
        }

        RabbitMQClientTests client = new RabbitMQClientTests(env);
        client.deleteIdleQueues(0);
    }


    public void deleteIdleQueues(int amountOfMessagesReady) {
        List<RabbitMQQueueInfo> queues = getAllQueues();

        for (RabbitMQQueueInfo queue : queues) {
            if (queue.getMessagesReady() >= amountOfMessagesReady &&
                    queue.getMessagesUnacknowledged() == 0 &&
                    queue.getConsumers() == 0) {
                log.info("🧹 Queue with 0 consumers and ≥ {} messages. Deleting: {}", amountOfMessagesReady, queue);
                deleteQueue(queue.getName());
            }
        }
    }

    public void deleteQueueFrom(final String bindingName) {
        try {
            List<RabbitMQBinding> bindings = getAllBindings();
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

        URI uri = UriComponentsBuilder.fromHttpUrl(url).build(true).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<RabbitMQQueueInfo[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, RabbitMQQueueInfo[].class);

        if (response.getBody() == null) throw new RuntimeException("No queues found");
        return List.of(response.getBody());
    }

    private List<RabbitMQBinding> getAllBindings() {
        String url = rabbitHost + "/api/bindings";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<RabbitMQBinding[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, RabbitMQBinding[].class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Failed to fetch bindings");
        }

        return List.of(response.getBody());
    }

    public void deleteQueue(String queueName) {
        String url = rabbitHost + "/api/queues/%2F/" + queueName;

        URI uri = UriComponentsBuilder.fromHttpUrl(url).build(true).toUri();
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
