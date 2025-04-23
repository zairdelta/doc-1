package com.woow.it;

import com.woow.WoowBaseTest;
import com.woow.axsalud.service.api.dto.HealthServiceProviderDTO;
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
}
