package com.woow.security.rabbitmq;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RabbitMQQueueInfo {
    private String name;
    @JsonProperty("messages_ready")
    private int messagesReady;
    @JsonProperty("messages_unacknowledged")
    private int messagesUnacknowledged;
    @JsonProperty("idle_since")
    private String idleSince;
    private int consumers;
}
