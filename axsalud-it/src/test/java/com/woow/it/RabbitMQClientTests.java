package com.woow.it;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class RabbitMQClientTests {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl = "https://0rbjt9.stackhero-network.com/api";
    private final String username = "admin";
    private final String password = "9KLwnaP17gIdi8CoDetZIlHMgaaVxkqf";

    public static void main(String[] args) {
        RabbitMQClientTests rabbitMQClientTests = new RabbitMQClientTests();
        rabbitMQClientTests.deleteBindingForTopic(
                "/",
                "consultation.1ef87843-6010-4231-bc33-f3df7360fa2b.session.e2ab39f6-332f-468d-9610-1e519104d4a7.control"
        );
    }

    public void deleteBindingForTopic(String vhost, String routingKeyToFind) {
        String encodedVhost = encodeVhost(vhost);
        String url = baseUrl + "/bindings/";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        } catch (Exception e) {
            System.err.println("❌ Error fetching bindings: " + e.getMessage());
            return;
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            System.out.println("❌ Failed to fetch bindings: " + response.getStatusCode());
            return;
        }

        JsonArray bindings = JsonParser.parseString(response.getBody()).getAsJsonArray();

        for (JsonElement element : bindings) {
            JsonObject binding = element.getAsJsonObject();
            String routingKey = binding.get("routing_key").getAsString();
            String source = binding.get("source").getAsString();
            String destination = binding.get("destination").getAsString();
            String destinationType = binding.get("destination_type").getAsString();

            if (routingKey.equals(routingKeyToFind) && "queue".equals(destinationType)) {
                System.out.printf("🎯 Match found — Exchange: %s, Queue: %s, RoutingKey: %s%n", source, destination, routingKey);

                String deleteUrl = String.format(
                        "%s/bindings/%s/e/%s/q/%s/%s",
                        baseUrl,
                        encodedVhost,
                        encode(source),
                        encode(destination),
                        encode(routingKey)
                );

                System.out.println("Deleting binding with URL: " + deleteUrl);

                try {
                    ResponseEntity<Void> deleteResp = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, Void.class);
                    System.out.println("✅ Delete status: " + deleteResp.getStatusCode());
                } catch (Exception e) {
                    System.err.println("❌ Failed to delete binding: " + e.getMessage());
                }
                return;
            }
        }

        System.out.println("⚠️ No matching binding found for routing key: " + routingKeyToFind);
    }

    private String encode(String val) {
        try {
            return URLEncoder.encode(val, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding not supported", e);
        }
    }

    private String encodeVhost(String vhost) {
        // RabbitMQ vhost '/' must be encoded as %2F, but not double-encoded!
        if ("/".equals(vhost)) return "%2F";
        return encode(vhost);
    }
}
