package com.woow.it;

import com.woow.WoowBaseTest;
import com.woow.axsalud.data.client.PatientAdditional;
import com.woow.axsalud.data.client.PatientData;
import com.woow.axsalud.service.api.dto.AxSaludUserDTO;
import com.woow.axsalud.service.api.dto.PatientViewDTO;
import com.woow.it.data.UserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.HashSet;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class AxSaludUserControllerTest extends WoowBaseTest {


    @Test
    void testUserCreationEndToEnd() {

        AxSaludUserDTO axSaludUserDTO = UserFactory.anyUser();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AxSaludUserDTO> request = new HttpEntity<>(axSaludUserDTO, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(getBaseUrl() + "woo_user/new", request, Void.class);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assertions.assertNotNull(response.getHeaders().getLocation());
        assertThat(response.getHeaders().getLocation().toString()).contains("/api/woo_user/realuser@woow.com");
    }

    @Test
    void testAddPatientData() {

        AxSaludUserDTO axSaludUserDTO = UserFactory.anyUser();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AxSaludUserDTO> request = new HttpEntity<>(axSaludUserDTO, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(getBaseUrl() + "woo_user/new", request, Void.class);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);

        PatientData patientData = new PatientData();
        patientData.setBuild(12.1f);
        patientData.setEmergencyContactName("EmergencyContactName1");
        patientData.setEmergencyContactNumber("844345345345");
        patientData.setHeight(13f);
        patientData.setWeight(90.1f);
        PatientAdditional patientAdditional = new PatientAdditional();
        patientData.setPatientAdditionalSet(new HashSet<>());
        patientAdditional.setBirth(LocalDate.now());
        patientAdditional.setName("AditionalPatient");
        patientData.getPatientAdditionalSet().add(patientAdditional);


        addAuthorizationHeader(axSaludUserDTO.getUserDtoCreate(), headers);
        HttpEntity<PatientData> requestPatientData = new HttpEntity<>(patientData, headers);

        ResponseEntity<Void> responsePatientData = restTemplate.exchange(
                getBaseUrl() + "woo_user/patientData",
                HttpMethod.PUT,
                requestPatientData,
                Void.class
        );

        Assertions.assertEquals(HttpStatus.OK, responsePatientData.getStatusCode());

        HttpEntity<Void> getRequest = new HttpEntity<>(headers);
        ResponseEntity<PatientViewDTO> getResponse = restTemplate.exchange(
                getBaseUrl() + "woo_user", HttpMethod.GET, getRequest, PatientViewDTO.class);

        // Step 4: Assert returned user data
        Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        Assertions.assertTrue(getResponse.getBody().getPatientData() != null);


    }

    @Test
    void testGetPatientInformationFromAuthenticatedUser() {
        // Step 1: Create user
        AxSaludUserDTO axSaludUserDTO = UserFactory.anyUser();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AxSaludUserDTO> request = new HttpEntity<>(axSaludUserDTO, headers);

        ResponseEntity<Void> postResponse = restTemplate.postForEntity(
                getBaseUrl() + "woo_user/new", request, Void.class);
        Assertions.assertEquals(HttpStatus.OK, postResponse.getStatusCode());

        headers.setContentType(MediaType.APPLICATION_JSON);
        addAuthorizationHeader(axSaludUserDTO.getUserDtoCreate(), headers);


        HttpEntity<Void> getRequest = new HttpEntity<>(headers);
        ResponseEntity<PatientViewDTO> getResponse = restTemplate.exchange(
                getBaseUrl() + "woo_user", HttpMethod.GET, getRequest, PatientViewDTO.class);

        // Step 4: Assert returned user data
        Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode());
    }

}
