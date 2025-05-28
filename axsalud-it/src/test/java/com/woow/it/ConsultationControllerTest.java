package com.woow.it;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Lists;
import com.woow.WoowBaseTest;
import com.woow.axsalud.common.WoowConstants;
import com.woow.axsalud.service.api.dto.*;
import com.woow.axsalud.service.api.messages.ConsultationEventDTO;
import com.woow.axsalud.service.api.messages.ConsultationMessageDTO;
import com.woow.it.data.HealthProviderFactory;
import com.woow.it.data.UserFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(response.getHeaders().getLocation());
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
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(response.getHeaders().getLocation());
        assertThat(response.getHeaders().getLocation().toString())
                .contains("/api/woo_user/realuser@woow.com");

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

        StompHeaders connectPatientnHeaders = new StompHeaders();
        // connectPatientnHeaders.add(WoowConstants.AUTHORIZATION_HEADER, JWT_TOKEN_PATIENT);

        WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();
        httpHeaders.add(WoowConstants.AUTHORIZATION_HEADER, JWT_TOKEN_PATIENT);

        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {};
        System.out.println("HTTP Headers: " + httpHeaders.toSingleValueMap());
        System.out.println("STOMP Headers: " + connectPatientnHeaders.toSingleValueMap());

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

        BlockingQueue<ConsultationMessageDTO> patientQueue = new ArrayBlockingQueue<>(5);

        CompletableFuture<StompSession> futureSession = stompClient
                .connectAsync(WS_URI, httpHeaders, connectPatientnHeaders, sessionHandler);

        StompSession session = futureSession.get(500, TimeUnit.SECONDS);

        session.subscribe("/user/queue/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Object.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {

                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.registerModule(new JavaTimeModule());

                    // Raw JSON string (only if Spring gave you a byte[])
                    String json = new String((byte[]) payload, StandardCharsets.UTF_8);

                    ConsultationEventDTO<ConsultationMessageDTO> event =
                            mapper.readValue(json, new TypeReference<ConsultationEventDTO<ConsultationMessageDTO>>() {});

                    System.out.println("Deserialized message: " + event.getPayload().getContent());
                    // Add to your queue or process further
                    patientQueue.offer(event.getPayload());

                } catch (Exception e) {
                    e.printStackTrace();
                    Assertions.fail("Failed to deserialize WebSocket message: " + e.getMessage());
                }

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
                                consultationDTOResponseEntity.getBody().getConsultationId() +
                                "/sessionId/" +
                                consultationDTOResponseEntity.getBody().getCurrentSessionIdIfExists() + "/doctor",
                        HttpMethod.PUT,
                        acceptPatientDoctorRequest,
                        ConsultationDTO.class);


        String JWT_TOKEN_DOCTOR =
                login(healthServiceProviderDTO.getUserDtoCreate().getUserName(),
                        healthServiceProviderDTO.getUserDtoCreate().getPassword());

        System.out.println("DOCTOR: " + JWT_TOKEN_DOCTOR);

        StompHeaders doctorStompHeaders = new StompHeaders();
        // doctorStompHeaders.add(WoowConstants.AUTHORIZATION_HEADER, JWT_TOKEN_DOCTOR);

        WebSocketHttpHeaders doctorHeaders = new WebSocketHttpHeaders();
        doctorHeaders.add(WoowConstants.AUTHORIZATION_HEADER, JWT_TOKEN_DOCTOR);

        StompSessionHandler doctorSessionHandler = new StompSessionHandlerAdapter() {};


        CompletableFuture<StompSession> futureDoctorSession = stompClient
                .connectAsync(WS_URI, doctorHeaders, doctorStompHeaders, doctorSessionHandler);
        StompSession doctorSession = futureDoctorSession.get(100, TimeUnit.SECONDS);

        BlockingQueue<ConsultationMessageDTO> doctorMessages = new ArrayBlockingQueue<>(5);

        doctorSession.subscribe("/user/queue/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Object.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {

                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.registerModule(new JavaTimeModule());

                    // Raw JSON string (only if Spring gave you a byte[])
                    String json = new String((byte[]) payload, StandardCharsets.UTF_8);

                    ConsultationEventDTO<ConsultationMessageDTO> event =
                            mapper.readValue(json, new TypeReference<ConsultationEventDTO<ConsultationMessageDTO>>() {});

                    System.out.println("Deserialized message: " + event.getPayload().getContent());
                    // Add to your queue or process further
                    doctorMessages.offer(event.getPayload());

                } catch (Exception e) {
                    e.printStackTrace();
                    Assertions.fail("Failed to deserialize WebSocket message: " + e.getMessage());
                }
            }
        });

        ConsultationMessageDTO message = new ConsultationMessageDTO();
        message.setConsultationId(consultationDTOResponseEntity
                .getBody().getConsultationId().toString());
        message.setContent("Hello from patient");
        message.setReceiver(healthServiceProviderDTO.getUserDtoCreate().getUserName());

        System.out.println("Patient Message Content");
        session.send("/app/consultation/" + consultationDTOResponseEntity
                .getBody().getConsultationId().toString() + "/session/" + consultationDTOResponseEntity.getBody().getCurrentSessionIdIfExists() + "/private", message);

        message = new ConsultationMessageDTO();
        message.setConsultationId(consultationDoctorDTOResponseEntity
                .getBody().getConsultationId().toString());
        message.setConsultationSessionId(consultationDoctorDTOResponseEntity.getBody().getCurrentSessionIdIfExists());
        message.setContent("Hello from Doctor");
        message.setReceiver(axSaludUserDTO.getUserDtoCreate().getUserName());
        System.out.println("Doctor Message");
        doctorSession.send("/app/consultation/" + consultationDoctorDTOResponseEntity
                .getBody().getConsultationId().toString() + "/session/" + consultationDoctorDTOResponseEntity
                .getBody().getCurrentSessionIdIfExists() + "/private", message);

        DoctorPrescriptionDTO doctorPrescriptionDTO = new DoctorPrescriptionDTO();
        doctorPrescriptionDTO.setNotasDeRecomendaciones("Notas Recomendaciones");
        doctorPrescriptionDTO.setDiagnostico("Diagnostico");
        doctorPrescriptionDTO.setRecetaMedica("recetamedica");
        doctorPrescriptionDTO.setComentariosMedicos("Comentarios Medicos");

        String consultationId = consultationDoctorDTOResponseEntity.getBody().getConsultationId().toString();
        String consultationSessionId = consultationDoctorDTOResponseEntity.getBody().getCurrentSessionIdIfExists();

        String addDoctorPrescriptionRequest = getBaseUrl() + "/consultation/" + consultationId +
                "/sessionId/" + consultationSessionId + "/doctorPrescription";

        List<DoctorPrescriptionDTO> doctorPrescriptionDTOS = Lists.newArrayList(doctorPrescriptionDTO);

        HttpEntity<List<DoctorPrescriptionDTO>> requestEntity = new HttpEntity<>(doctorPrescriptionDTOS, headersDoctor);

        ResponseEntity<Void> responseCreatePrescription = restTemplate.exchange(
                addDoctorPrescriptionRequest,
                HttpMethod.PUT,
                requestEntity,
                Void.class
        );

        String getPrescriptionUrl = getBaseUrl() + "/doctor/patient/" +
                axSaludUserDTO.getUserDtoCreate().getUserName() + "/docPrescriptions";

        HttpEntity<Void> getRequestEntity = new HttpEntity<>(headersDoctor);

        ResponseEntity<DoctorPrescriptionViewDTO[]> responseGetPrescriptions = restTemplate.exchange(
                getPrescriptionUrl,
                HttpMethod.GET,
                getRequestEntity,
                DoctorPrescriptionViewDTO[].class
        );

        assertEquals(HttpStatus.OK, responseGetPrescriptions.getStatusCode());
        assertNotNull(responseGetPrescriptions.getBody());
        assertTrue(responseGetPrescriptions.getBody().length > 0);

        DoctorPrescriptionViewDTO retrieved = responseGetPrescriptions.getBody()[0];
        assertEquals(doctorPrescriptionDTO.getDiagnostico(),
                retrieved.getDoctorPrescriptionDTO().getDiagnostico());


        HttpHeaders consultationFetchHeaders = new HttpHeaders();
        consultationFetchHeaders.setContentType(MediaType.APPLICATION_JSON);
        addAuthorizationHeader(axSaludUserDTO.getUserDtoCreate(), consultationFetchHeaders);

        HttpEntity<Void> fetchConsultationsRequest = new HttpEntity<>(consultationFetchHeaders);

        ResponseEntity<ConsultationDTO[]> consultationsResponse = restTemplate.exchange(
                getBaseUrl() + "woo_user/consultations",
                HttpMethod.GET,
                fetchConsultationsRequest,
                ConsultationDTO[].class
        );

        assertEquals(HttpStatus.OK, consultationsResponse.getStatusCode());
        assertNotNull(consultationsResponse.getBody());
        assertTrue(consultationsResponse.getBody().length > 0);

        for (ConsultationDTO consultation : consultationsResponse.getBody()) {
            System.out.println("Consultation ID: " + consultation.getConsultationId());
        }

        Thread.sleep(5000);

        List<ConsultationMessageDTO> patientMessagesList = new ArrayList<>();
        patientQueue.drainTo(patientMessagesList, 10);

        List<ConsultationMessageDTO> doctorMessagesList = new ArrayList<>();
        doctorMessages.drainTo(doctorMessagesList, 10);

        patientMessagesList.stream()
                .anyMatch(msg -> "WELCOME".equalsIgnoreCase(msg.getContent()));
        doctorMessagesList.stream()
                .anyMatch(msg -> "Hello from patient".equalsIgnoreCase(msg.getContent()));


        patientMessagesList.forEach(consultationMessage -> System.out.println("PatientMessages: " + consultationMessage.getContent()));
        doctorMessagesList.forEach(consultationMessage -> System.out.println("Doctor Messages: " + consultationMessage.getContent()));

        session.disconnect();
        doctorSession.disconnect();
    }

    @Test
    void shouldUploadConsultationDocument() throws Exception {
        AxSaludUserDTO axSaludUserDTO = UserFactory.anyUser();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AxSaludUserDTO> request = new HttpEntity<>(axSaludUserDTO, headers);

        // Create user
        ResponseEntity<Void> response = restTemplate.postForEntity(getBaseUrl() + "woo_user/new", request, Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Login and get JWT
        String JWT_TOKEN = login(axSaludUserDTO.getUserDtoCreate().getUserName(), axSaludUserDTO.getUserDtoCreate().getPassword());

        // Create consultation
        SymptomsDTO symptomsDTO = new SymptomsDTO();
        symptomsDTO.setText("My Symtoms are headache, temperature");
        addAuthorizationHeader(axSaludUserDTO.getUserDtoCreate(), headers);
        HttpEntity<SymptomsDTO> consultationRequest = new HttpEntity<>(symptomsDTO, headers);
        ResponseEntity<ConsultationDTO> consultationResponse =
                restTemplate.postForEntity(getBaseUrl() + "consultation", consultationRequest, ConsultationDTO.class);

        String consultationId = consultationResponse.getBody()
                .getConsultationId();

        String consultationSessionId = consultationResponse.getBody()
                .getCurrentSessionIdIfExists();

        // Load file from resources (src/test/resources/sample.pdf for example)
        File file = new File(Objects.requireNonNull(getClass()
                .getClassLoader().getResource("storageExample.txt")).getFile());
        FileSystemResource fileResource = new FileSystemResource(file);

        // Create multipart request
        HttpHeaders multipartHeaders = new HttpHeaders();
        multipartHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        addAuthorizationHeader(axSaludUserDTO.getUserDtoCreate(), multipartHeaders);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        HttpEntity<MultiValueMap<String, Object>> multipartRequest =
                new HttpEntity<>(body, multipartHeaders);

        ResponseEntity<FileResponseDTO> uploadResponse = restTemplate.postForEntity(
                getBaseUrl() + "consultation/" + consultationId + "/sessionId/"
                        + consultationSessionId + "/file", multipartRequest, FileResponseDTO.class);

        assertEquals(HttpStatus.OK, uploadResponse.getStatusCode());
        assertNotNull(uploadResponse.getBody());
    }


    @Test
    void shouldDownloadConsultationDocument() throws Exception {
        // Paso 1: Crear usuario y consulta como ya lo haces
        AxSaludUserDTO axSaludUserDTO = UserFactory.anyUser();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AxSaludUserDTO> request = new HttpEntity<>(axSaludUserDTO, headers);
        restTemplate.postForEntity(getBaseUrl() + "woo_user/new", request, Void.class);

        SymptomsDTO symptomsDTO = new SymptomsDTO();
        symptomsDTO.setText("Headache and fever");
        addAuthorizationHeader(axSaludUserDTO.getUserDtoCreate(), headers);
        HttpEntity<SymptomsDTO> consultationRequest = new HttpEntity<>(symptomsDTO, headers);
        ResponseEntity<ConsultationDTO> consultationResponse = restTemplate.postForEntity(
                getBaseUrl() + "consultation", consultationRequest, ConsultationDTO.class);
        String consultationId = consultationResponse.getBody().getConsultationId().toString();
        String consultationSessionId = consultationResponse
                .getBody().getCurrentSessionIdIfExists().toString();

        File file = new File(Objects.requireNonNull(getClass()
                .getClassLoader().getResource("storageExample.txt")).getFile());
        FileSystemResource fileResource = new FileSystemResource(file);

        HttpHeaders uploadHeaders = new HttpHeaders();
        uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        addAuthorizationHeader(axSaludUserDTO.getUserDtoCreate(), uploadHeaders);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        HttpEntity<MultiValueMap<String, Object>> uploadRequest =
                new HttpEntity<>(body, uploadHeaders);

        ResponseEntity<FileResponseDTO> fileUploadedResponse = restTemplate.postForEntity(
                getBaseUrl() + "consultation/" + consultationId + "/sessionId/"
                        + consultationSessionId + "/file", uploadRequest, FileResponseDTO.class);

        assertEquals(HttpStatus.OK, fileUploadedResponse.getStatusCode());

        Long fileId = fileUploadedResponse.getBody().getId();

        HttpHeaders downloadHeaders = new HttpHeaders();
        addAuthorizationHeader(axSaludUserDTO.getUserDtoCreate(), downloadHeaders);

        HttpEntity<Void> downloadRequest = new HttpEntity<>(downloadHeaders);

        ResponseEntity<FileResponseDTO> downloadResponse = restTemplate.exchange(
                getBaseUrl() + "consultation/" + consultationId + "/sessionId/"
                        + consultationSessionId + "/file/" + fileId,
                HttpMethod.GET,
                downloadRequest,
                FileResponseDTO.class);

        assertEquals(HttpStatus.OK, downloadResponse.getStatusCode());
        assertTrue(downloadResponse.getBody().getUrl().contains("https:")); // URL firmada
    }

}
