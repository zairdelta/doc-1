package com.woow.security.api;

public interface WooBoTokenService {
    String createTokenForUserId(final String userName);

    boolean validateToken(final String userName, final String token);
}
