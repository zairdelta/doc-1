package com.woow;

import com.woow.axsalud.common.WoowConstants;
import com.woow.core.service.api.UserDtoCreate;
import com.woow.it.client.UserLoginRestClient;
import com.woow.it.config.TestRestTemplateConfig;
import com.woow.security.api.JwtRequest;
import com.woow.security.api.JwtResponse;
import kong.unirest.core.Unirest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;


@SpringBootTest(classes = {AxSaludAPP.class, TestRestTemplateConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = "server.port=8443")
@Sql(scripts = {"classpath:sql/clean-up.sql", "classpath:sql/set-up.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestRestTemplateConfig.class)
public class WoowBaseTest {

    public static final String BEARER = "Bearer ";
    private String authToken = "";
    protected static int port = 8443;

    @Autowired
    protected RestTemplate restTemplate;

    private UserLoginRestClient userLoginRestClient = new UserLoginRestClient(port);

    @Autowired
    TestRestTemplateConfig config;

    @BeforeEach
    void overrideRestTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        this.restTemplate = config.getRestTemplate(); // explicitly override
        Unirest.config()
                .verifySsl(false);
    }

    public String getBaseUrl() {
        return "https://localhost:" + port + "/api/";
    }

    @BeforeAll
    public static void disableSSL() {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
    }

    public void addAuthorizationHeader(UserDtoCreate wooUserDTO, HttpHeaders headers) {
        String bearerToken = login(wooUserDTO);
        headers.set(WoowConstants.AUTHORIZATION_HEADER, bearerToken);
    }

    public String login(UserDtoCreate wooUserDTO) {
        return login(wooUserDTO.getUserName(), wooUserDTO.getPassword());
    }

    public String login(final String userName, final String password) {
        JwtRequest jwtRequest = new JwtRequest();
        jwtRequest.setUsername(userName);
        jwtRequest.setPassword(password);
        JwtResponse jwtResponse = userLoginRestClient.login(jwtRequest, WoowConstants.HTTP_200_CODE);
        this.authToken = BEARER + jwtResponse.getToken();
        return authToken;
    }

}
