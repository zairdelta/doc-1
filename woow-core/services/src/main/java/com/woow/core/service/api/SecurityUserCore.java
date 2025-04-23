package com.woow.core.service.api;

import com.woow.security.api.SecurityUser;

import java.util.List;

public class SecurityUserCore implements SecurityUser {
    public SecurityUserCore(long userId, List<String> roles,
                            String userName, String password, String tenantId) {
        this.userId = userId;
        this.roles = roles;
        this.userName = userName;
        this.password = password;
        this.tenantId = tenantId;
    }

    private long userId;
    private List<String> roles;
    private String userName;
    private String password;
    private String tenantId;

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public List<String> getSecurityRoles() {
        return roles;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }
}
