package com.woow.serviceprovider.impl;

import com.google.gson.Gson;
import com.woow.serviceprovider.api.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

@Service
@Slf4j
public class ServiceProviderClientImpl implements ServiceProviderClient {
    @Override
    public TelemedicineResponse isHIDValid(ServiceProviderRequestDTO serviceProviderRequestDTO,
                                           String hid) throws ServiceProviderClientException{

        try {
            log.info("Preparing request for HID validation, service Name: {}",
                    serviceProviderRequestDTO.getServiceName());
            URI uri = new URI(serviceProviderRequestDTO.getUrl()
                    + ServiceProvider.VALIDATE_HID_NUMBER_URL
                    + hid);

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(ServiceProvider.API_KEY, serviceProviderRequestDTO.getApiKey());

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<String> webHookResponse = restTemplate.exchange(
                    uri, HttpMethod.GET, request, String.class);

            log.info("Response code = {}, Body = {}",
                    webHookResponse.getStatusCode(), webHookResponse.getBody());

            if (webHookResponse.getStatusCode() == HttpStatus.OK) {
                Gson gson = new Gson();
                return gson.fromJson(webHookResponse.getBody(), TelemedicineResponse.class);
            } else {
                log.warn("Non-OK status: {}", webHookResponse.getStatusCode());
                throw new ServiceProviderClientException("Response from external Service not 200", 401);
            }

        } catch (Exception e) {
            log.error("Error validating HID", e);
            throw new ServiceProviderClientException("Exception in, " +
                    "Response from external Service not 200 " + e.getMessage(), 401);
        }
    }

    @Override
    public List<TelemedicineAllUsersDTO> getAllUsers() {
        return null;
    }
}
