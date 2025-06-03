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
    private final String username = "admin";
    private final String password = "9KLwnaP17gIdi8CoDetZIlHMgaaVxkqf";
    private final String VHOST = "/";

    public static void main(String[] args) {
        try {
            RabbitMQClientTests rabbitMQClientTests = new RabbitMQClientTests();
            List<RabbitMQBinding> bindings = rabbitMQClientTests.getAllBindings();
            log.info("🔗 Total bindings: {}", bindings.size());

            for (RabbitMQBinding binding : bindings) {
                log.info("Binding => source: {}, destination: {}, destination_type: {}, routing_key: {}",
                        binding.getSource(),
                        binding.getDestination(),
                        binding.getDestinationType(),
                        binding.getRoutingKey()
                );

                rabbitMQClientTests.deleteQueue(binding.getDestination());
            }
        } catch (Exception e) {
            log.error("❌ Error fetching bindings: {}", e.getMessage(), e);
        }
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
