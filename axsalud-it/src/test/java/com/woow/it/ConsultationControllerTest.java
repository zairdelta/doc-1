package com.woow.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woow.WoowBaseTest;
import com.woow.axsalud.common.WoowConstants;
import com.woow.axsalud.service.api.dto.*;
import com.woow.it.data.HealthProviderFactory;
import com.woow.it.data.UserFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import javax.net.ssl.*;
import java.lang.reflect.Type;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ConsultationControllerTest extends WoowBaseTest {

    @Autowired
    private ObjectMapper objectMapper;

    private static final String WS_URI = "wss://localhost:8443/ws"; // adjust if needed

    @Test
    void testHealthProviderUserCreationEndToEnd() {

        AxSaludUserDTO axSaludUserDTO = UserFactory.anyUser();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AxSaludUserDTO> request = new HttpEntity<>(axSaludUserDTO, headers);

        ResponseEntity<Void> response = restTemplate
                .postForEntity(getBaseUrl() + "woo_user/new", request, Void.class);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assertions.assertNotNull(response.getHeaders().getLocation());
        assertThat(response.getHeaders().getLocation().toString()).contains("/api/woo_user/realuser@woow.com");


        SymptomsDTO symptomsDTO = new SymptomsDTO();
        symptomsDTO.setText("My Symtoms are headache, temperature");

        HttpEntity<SymptomsDTO> consultationRequest =
                new HttpEntity<>(symptomsDTO, headers);
        addAuthorizationHeader(axSaludUserDTO.getUserDtoCreate(), headers);

        ResponseEntity<ConsultationDTO> consultationDTOResponseEntity =
                restTemplate.postForEntity(getBaseUrl() + "consultation",
                        consultationRequest, ConsultationDTO.class);

        System.out.println(consultationDTOResponseEntity.getBody());

    }

    @Test
    void shouldReceivePrivateMessage() throws Exception {

        AxSaludUserDTO axSaludUserDTO = UserFactory.anyUser();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AxSaludUserDTO> request = new HttpEntity<>(axSaludUserDTO, headers);

        ResponseEntity<Void> response = restTemplate
                .postForEntity(getBaseUrl() + "woo_user/new", request, Void.class);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assertions.assertNotNull(response.getHeaders().getLocation());
        assertThat(response.getHeaders().getLocation().toString()).contains("/api/woo_user/realuser@woow.com");

        SymptomsDTO symptomsDTO = new SymptomsDTO();
        symptomsDTO.setText("My Symtoms are headache, temperature");

        HttpEntity<SymptomsDTO> consultationRequest =
                new HttpEntity<>(symptomsDTO, headers);
        addAuthorizationHeader(axSaludUserDTO.getUserDtoCreate(), headers);

        ResponseEntity<ConsultationDTO> consultationDTOResponseEntity =
                restTemplate.postForEntity(getBaseUrl() + "consultation",
                        consultationRequest, ConsultationDTO.class);


        String JWT_TOKEN_PATIENT =
                login(axSaludUserDTO.getUserDtoCreate().getUserName(),
                        axSaludUserDTO.getUserDtoCreate().getPassword());

        System.out.println("JWT_TOKEN_PATIENT: " + JWT_TOKEN_PATIENT);

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add(WoowConstants.AUTHORIZATION_HEADER, JWT_TOKEN_PATIENT);

        WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();
        httpHeaders.add(WoowConstants.AUTHORIZATION_HEADER, JWT_TOKEN_PATIENT);

        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {};
        System.out.println("HTTP Headers: " + httpHeaders.toSingleValueMap());
        System.out.println("STOMP Headers: " + connectHeaders.toSingleValueMap());

        // Install the all-trusting trust manager
        TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                return true;
            }
        };

        // Create an SSLContext that bypasses SSL validation
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        StandardWebSocketClient standardWebSocketClient = new StandardWebSocketClient();
        standardWebSocketClient.setSslContext(sslContext);
        WebSocketStompClient stompClient = new WebSocketStompClient(standardWebSocketClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        BlockingQueue<ConsultationMessage> patientQueue = new ArrayBlockingQueue<>(5);

        CompletableFuture<StompSession> futureSession = stompClient
                .connectAsync(WS_URI, httpHeaders, connectHeaders, sessionHandler);

        StompSession session = futureSession.get(5, TimeUnit.SECONDS);

        session.subscribe("/user/queue/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ConsultationMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                patientQueue.offer((ConsultationMessage) payload);
            }
        });


        HealthServiceProviderDTO healthServiceProviderDTO =
                HealthProviderFactory.anyHealthProvider();
        HttpHeaders headersDoctor = new HttpHeaders();
        headersDoctor.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<HealthServiceProviderDTO> doctorCreateRequest =
                new HttpEntity<>(healthServiceProviderDTO, headers);

        ResponseEntity<Void> doctorResponse =
                restTemplate.postForEntity(getBaseUrl() + "health_provider/new",
                        doctorCreateRequest, Void.class);


        addAuthorizationHeader(healthServiceProviderDTO
                        .getUserDtoCreate(),
                headersDoctor);

        HttpEntity<ConsultationDTO> acceptPatientDoctorRequest =
                new HttpEntity<>( headersDoctor);


        ResponseEntity<ConsultationDTO> consultationDoctorDTOResponseEntity =
                restTemplate.exchange(getBaseUrl() + "consultation/" +
                                consultationDTOResponseEntity.getBody().getConsultationId() + "/doctor",
                        HttpMethod.PUT,
                        acceptPatientDoctorRequest,
                        ConsultationDTO.class);


        String JWT_TOKEN_DOCTOR =
                login(healthServiceProviderDTO.getUserDtoCreate().getUserName(),
                        healthServiceProviderDTO.getUserDtoCreate().getPassword());

        System.out.println("DOCTOR: " + JWT_TOKEN_DOCTOR);

        StompHeaders doctorStompHeaders = new StompHeaders();
        doctorStompHeaders.add(WoowConstants.AUTHORIZATION_HEADER, JWT_TOKEN_DOCTOR);

        WebSocketHttpHeaders doctorHeaders = new WebSocketHttpHeaders();
        doctorHeaders.add(WoowConstants.AUTHORIZATION_HEADER, JWT_TOKEN_DOCTOR);

        StompSessionHandler doctorSessionHandler = new StompSessionHandlerAdapter() {};


        CompletableFuture<StompSession> futureDoctorSession = stompClient
                .connectAsync(WS_URI, doctorHeaders, doctorStompHeaders, doctorSessionHandler);
        StompSession doctorSession = futureDoctorSession.get(5, TimeUnit.SECONDS);

        BlockingQueue<ConsultationMessage> doctorMessages = new ArrayBlockingQueue<>(5);

        doctorSession.subscribe("/user/queue/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ConsultationMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                doctorMessages.offer((ConsultationMessage) payload);
            }
        });

        ConsultationMessage message = new ConsultationMessage();
        message.setConsultationId(consultationDTOResponseEntity
                .getBody().getConsultationId().toString());
        message.setContent("Hello from patient");
        message.setReceiver(axSaludUserDTO.getUserDtoCreate().getUserName());

        session.send("/app/consultation/" + consultationDTOResponseEntity
                .getBody().getConsultationId().toString() +"/private", message);

        List<ConsultationMessage> patientMessagesList = new ArrayList<>();
        patientQueue.drainTo(patientMessagesList, 10);

        List<ConsultationMessage> doctorMessagesList = new ArrayList<>();
        doctorMessages.drainTo(doctorMessagesList, 10);

        patientMessagesList.stream()
                        .anyMatch(msg -> "WELCOME".equalsIgnoreCase(msg.getContent()));
        doctorMessagesList.stream()
                .anyMatch(msg -> "Hello from patient".equalsIgnoreCase(msg.getContent()));

    }
}
