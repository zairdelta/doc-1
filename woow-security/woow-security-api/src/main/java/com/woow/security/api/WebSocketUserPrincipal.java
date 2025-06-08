package com.woow.security.api;


import java.security.Principal;
import java.util.List;

public class WebSocketUserPrincipal implements Principal {
    private final String name;
    private final List<String> roles;

    public WebSocketUserPrincipal(String name, List<String> roles) {
        this.name = name;
        this.roles = roles;
    }

    @Override
    public String getName() {
        return name;
    }

    public List<String> getRoles() {
        return roles;
    }
}
