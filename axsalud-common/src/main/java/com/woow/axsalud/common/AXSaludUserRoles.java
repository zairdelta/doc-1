package com.woow.axsalud.common;

public enum AXSaludUserRoles {

    ADMIN("ADMIN"),
    DOCTOR("DOCTOR"),
    USER("USER");


    private String role;
    private AXSaludUserRoles(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
