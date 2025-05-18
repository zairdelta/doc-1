package com.woow.it;

import com.woow.WoowBaseTest;
import com.woow.axsalud.data.client.PatientAdditional;
import com.woow.axsalud.data.client.PatientData;
import com.woow.axsalud.service.api.dto.*;
import com.woow.core.service.api.UserUpdateDto;
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
        patientData.setPreexistences("Preexistences");
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

        Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        Assertions.assertTrue(getResponse.getBody().getPatientDataDTO() != null);


    }

    @Test
    void testGetPatientInformationFromAuthenticatedUser() {

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

        Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode());
    }

    @Test
    void testUpdatePatientDataWithAdditionalUsers() {

        AxSaludUserDTO axSaludUserDTO = UserFactory.anyUser();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AxSaludUserDTO> createRequest = new HttpEntity<>(axSaludUserDTO, headers);

        ResponseEntity<Void> createResponse = restTemplate.postForEntity(
                getBaseUrl() + "woo_user/new", createRequest, Void.class);

        Assertions.assertEquals(HttpStatus.OK, createResponse.getStatusCode());

        // Step 2: Prepare the update DTO
        AxSaludUserUpdateDTO updateDTO = new AxSaludUserUpdateDTO();

        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setName("UpdatedName");
        userUpdateDto.setLastName("UpdatedLastName");
        userUpdateDto.setEmail(axSaludUserDTO.getUserDtoCreate().getEmail());
        userUpdateDto.setUserName(axSaludUserDTO.getUserDtoCreate().getUserName());
        userUpdateDto.setCity("UpdatedCity");
        updateDTO.setUserUpdateDto(userUpdateDto);

        // Prepare updated patient data
        PatientDataUpdateDTO patientDataUpdateDTO = new PatientDataUpdateDTO();
        PatientDataDTO patientDataDTO = new PatientDataDTO();
        patientDataDTO.setHeight(180f);
        patientDataDTO.setWeight(75f);
        patientDataDTO.setPreexistences("Updated Preexistences");
        patientDataDTO.setEmergencyContactName("Updated Emergency Contact");
        patientDataDTO.setEmergencyContactNumber("9999999999");

        // Include patient additional
        PatientAdditionalDTO additional = new PatientAdditionalDTO();
        additional.setName("Child1");
        additional.setBirth(LocalDate.of(2010, 1, 1));
        patientDataDTO.getPatientAdditionalSet().add(additional);

        patientDataUpdateDTO.setPatientDataDTO(patientDataDTO);
        updateDTO.setPatientDataUpdateDTO(patientDataUpdateDTO);

        // Step 3: Send update
        addAuthorizationHeader(axSaludUserDTO.getUserDtoCreate(), headers);
        HttpEntity<AxSaludUserUpdateDTO> updateRequest = new HttpEntity<>(updateDTO, headers);

        ResponseEntity<Void> updateResponse = restTemplate.exchange(
                getBaseUrl() + "woo_user",
                HttpMethod.PUT,
                updateRequest,
                Void.class
        );

        Assertions.assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        // Step 4: Verify with GET
        HttpEntity<Void> getRequest = new HttpEntity<>(headers);
        ResponseEntity<PatientViewDTO> getResponse = restTemplate.exchange(
                getBaseUrl() + "woo_user", HttpMethod.GET, getRequest, PatientViewDTO.class);

        Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode());

        PatientViewDTO patientView = getResponse.getBody();
        Assertions.assertNotNull(patientView);
        Assertions.assertEquals("UpdatedName", patientView.getName());
        Assertions.assertEquals("UpdatedCity", patientView.getCity());
        Assertions.assertEquals(180f, patientView.getPatientDataDTO().getHeight());
        Assertions.assertEquals("Updated Emergency Contact", patientView.getPatientDataDTO().getEmergencyContactName());

        // Assert one patient additional entry
        Assertions.assertFalse(patientView.getPatientDataDTO().getPatientAdditionalSet().isEmpty());
        Assertions.assertEquals("Child1", patientView.getPatientDataDTO().getPatientAdditionalSet().get(0).getName());
    }

    @Test
    void testUpdatePatientData() {

        AxSaludUserDTO axSaludUserDTO = UserFactory.anyUser();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AxSaludUserDTO> createRequest = new HttpEntity<>(axSaludUserDTO, headers);

        ResponseEntity<Void> createResponse = restTemplate.postForEntity(
                getBaseUrl() + "woo_user/new", createRequest, Void.class);

        Assertions.assertEquals(HttpStatus.OK, createResponse.getStatusCode());

        AxSaludUserUpdateDTO updateDTO = new AxSaludUserUpdateDTO();
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setName("UpdatedName");
        userUpdateDto.setLastName("UpdatedLastName");
        userUpdateDto.setEmail(axSaludUserDTO.getUserDtoCreate().getEmail());  // keep the same
        userUpdateDto.setUserName(axSaludUserDTO.getUserDtoCreate().getUserName()); // same
        userUpdateDto.setCity("UpdatedCity");
        updateDTO.setUserUpdateDto(userUpdateDto);

        PatientDataUpdateDTO patientDataUpdateDTO = new PatientDataUpdateDTO();
        PatientDataDTO patientDataDTO = new PatientDataDTO();
        patientDataDTO.setHeight(180f);
        patientDataDTO.setWeight(75f);
        patientDataDTO.setPreexistences("Updated Preexistences");
        patientDataDTO.setEmergencyContactName("Updated Emergency Contact");
        patientDataDTO.setEmergencyContactNumber("9999999999");
        patientDataDTO.setAlcohol("Alcahol");
        patientDataUpdateDTO.setPatientDataDTO(patientDataDTO);
        updateDTO.setPatientDataUpdateDTO(patientDataUpdateDTO);

        addAuthorizationHeader(axSaludUserDTO.getUserDtoCreate(), headers);

        HttpEntity<AxSaludUserUpdateDTO> updateRequest = new HttpEntity<>(updateDTO, headers);

        ResponseEntity<Void> updateResponse = restTemplate.exchange(
                getBaseUrl() + "woo_user",
                HttpMethod.PUT,
                updateRequest,
                Void.class
        );

        Assertions.assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        // Step 5: Verify the update by doing a GET
        HttpEntity<Void> getRequest = new HttpEntity<>(headers);
        ResponseEntity<PatientViewDTO> getResponse = restTemplate.exchange(
                getBaseUrl() + "woo_user", HttpMethod.GET, getRequest, PatientViewDTO.class);

        Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        Assertions.assertEquals("Alcahol", getResponse.getBody().getPatientDataDTO().getAlcohol());
        Assertions.assertEquals("UpdatedName", getResponse.getBody().getName());
        Assertions.assertEquals("UpdatedCity", getResponse.getBody().getCity());
        Assertions.assertEquals(180f, getResponse.getBody().getPatientDataDTO().getHeight());
        Assertions.assertEquals("Updated Emergency Contact", getResponse.getBody().getPatientDataDTO().getEmergencyContactName());
    }

}
