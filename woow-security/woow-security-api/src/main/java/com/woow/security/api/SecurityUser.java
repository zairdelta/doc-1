package com.woow.security.api;

import java.util.List;

public interface SecurityUser {

    long getUserId();
    List<String> getSecurityRoles();

    String getUserName();
    String getPassword();
    String getTenantId();
}
