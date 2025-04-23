package com.woow.security.api;

import com.woow.security.api.exception.JwtBlacklistException;

public interface BlackListService {
    boolean isTokenInBl(final String token);

    void addEntry(final String token) throws JwtBlacklistException ;

    void deleteEntry(final String token) ;
}
