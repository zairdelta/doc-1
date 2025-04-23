package com.woow.serviceprovider.api;

import lombok.Data;

@Data
public class ServiceProviderRequestDTO {
    private String serviceName;
    private String url;
    private String apiKey;
}
