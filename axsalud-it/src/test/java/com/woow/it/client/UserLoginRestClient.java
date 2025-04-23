package com.woow.it.client;

import com.woow.axsalud.common.WoowConstants;
import com.woow.security.api.JwtRequest;
import com.woow.security.api.JwtResponse;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;

import static org.springframework.test.util.AssertionErrors.assertEquals;

public class UserLoginRestClient extends WoowRestClientAbstract {

    public UserLoginRestClient(int serverPort) {
        super(serverPort);
    }

    public JwtResponse login(final JwtRequest jwtRequest, int validateErrorCode) {
        final String jwtRequestJson = gson.toJson(jwtRequest);

        final HttpResponse<String> result = Unirest
                .post(WoowConstants.SERVER_URL + serverPort +
                        "/api/woo_user/authenticate/ms_login?g-recaptcha-response=testRresponse")
                .header(WoowConstants.CONTENT_TYPE, WoowConstants.APPLICATION_JSON)
                .body(jwtRequestJson)
                .asString();

        result.getHeaders()
                .all()
                .stream()
                .forEach(stringListEntry -> {System.out.println(stringListEntry.getName() + " : " + stringListEntry.getValue());});
        assertEquals("Create failed", validateErrorCode, result.getStatus());

        return gson.fromJson(result.getBody(), JwtResponse.class);
    }

}
