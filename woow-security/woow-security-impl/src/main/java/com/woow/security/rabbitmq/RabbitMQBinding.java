package com.woow.security.rabbitmq;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RabbitMQBinding {
    private String source;

    private String destination;

    @JsonProperty("destination_type")
    private String destinationType;

    @JsonProperty("routing_key")
    private String routingKey;

    private String vhost;

}
