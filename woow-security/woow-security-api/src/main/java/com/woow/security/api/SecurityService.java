package com.woow.security.api;

public interface SecurityService {
    SecurityUser findByUserName(String username);
}
