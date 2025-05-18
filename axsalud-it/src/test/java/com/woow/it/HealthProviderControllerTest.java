package com.woow.it;

import com.woow.WoowBaseTest;
import com.woow.axsalud.service.api.dto.DoctorDataDTO;
import com.woow.axsalud.service.api.dto.HealthServiceProviderDTO;
import com.woow.axsalud.service.api.dto.HealthServiceProviderUpdateDTO;
import com.woow.core.service.api.UserUpdateDto;
import com.woow.it.data.HealthProviderFactory;
import com.woow.it.data.UserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class HealthProviderControllerTest extends WoowBaseTest {
    @Test
    void testHealthProviderUserCreationEndToEnd() {

        HealthServiceProviderDTO healthServiceProviderDTO =
                HealthProviderFactory.anyHealthProvider();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        addAuthorizationHeader(UserFactory
                .master()
                .getUserDtoCreate(),
                headers);


        HttpEntity<HealthServiceProviderDTO> request =
                new HttpEntity<>(healthServiceProviderDTO, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(getBaseUrl() + "health_provider/new", request, Void.class);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assertions.assertNotNull(response.getHeaders().getLocation());
        assertThat(response.getHeaders().getLocation().toString()).contains("/api/woo_user/doctor@woow.com");
    }

    @Test
    void testHealthProviderUserUpdate() {

        // Step 1: Create a health provider
        HealthServiceProviderDTO healthServiceProviderDTO = HealthProviderFactory.anyHealthProvider();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        addAuthorizationHeader(UserFactory
                        .master()
                        .getUserDtoCreate(),
                headers);

        HttpEntity<HealthServiceProviderDTO> createRequest =
                new HttpEntity<>(healthServiceProviderDTO, headers);

        ResponseEntity<Void> createResponse = restTemplate.postForEntity(
                getBaseUrl() + "health_provider/new", createRequest, Void.class);

        Assertions.assertEquals(HttpStatus.OK, createResponse.getStatusCode());

        // Step 2: Prepare update data
        HealthServiceProviderUpdateDTO updateDTO = new HealthServiceProviderUpdateDTO();
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setName("UpdatedHealthProviderName");
        userUpdateDto.setLastName("UpdatedLastName");
        userUpdateDto.setCity("UpdatedCity");
        userUpdateDto.setEmail(healthServiceProviderDTO.getUserDtoCreate().getEmail());
        userUpdateDto.setUserName(healthServiceProviderDTO.getUserDtoCreate().getUserName());
        updateDTO.setUserUpdateDto(userUpdateDto);

        updateDTO.setWelcomeMessage("Welcome to my updated health service!");

        DoctorDataDTO doctorDataDTO = new DoctorDataDTO();
        doctorDataDTO.setLicenseNumber("UPDATED-123");
        doctorDataDTO.setSpeciality("UpdatedSpeciality");
        doctorDataDTO.setUniversity("UpdatedUniversity");
        updateDTO.setDoctorDataDTO(doctorDataDTO);

        // Step 3: Login as the new health provider user
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        addAuthorizationHeader(healthServiceProviderDTO.getUserDtoCreate(), headers);

        HttpEntity<HealthServiceProviderUpdateDTO> updateRequest =
                new HttpEntity<>(updateDTO, headers);

        ResponseEntity<Void> updateResponse = restTemplate.exchange(
                getBaseUrl() + "health_provider",
                HttpMethod.PUT,
                updateRequest,
                Void.class
        );

        Assertions.assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        // Step 4: Verify the update by doing a GET
        HttpEntity<Void> getRequest = new HttpEntity<>(headers);
        ResponseEntity<HealthServiceProviderDTO> getResponse = restTemplate.exchange(
                getBaseUrl() + "health_provider", HttpMethod.GET, getRequest, HealthServiceProviderDTO.class);

        Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        Assertions.assertEquals("UpdatedHealthProviderName", getResponse.getBody().getUserDtoCreate().getName());
        Assertions.assertEquals("UpdatedCity", getResponse.getBody().getUserDtoCreate().getCity());
        Assertions.assertEquals("Welcome to my updated health service!", getResponse.getBody().getWelcomeMessage());
        Assertions.assertEquals("UPDATED-123", getResponse.getBody().getDoctorData().getLicenseNumber());
    }

}
