package com.woow.security.api;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class WooSecurityUserDetails implements UserDetails {


    private String username;
    private String password;

    private String tenantId;
    private long user_id;
    private Collection<? extends GrantedAuthority> authorities;

    public WooSecurityUserDetails(String username) {
        this.username = username;
    }

    public WooSecurityUserDetails(long user_id, String username,
                                  String password, Collection<? extends GrantedAuthority> authorities,
        String tenantId) {
        this.username = username;
        this.password = password;
        this.user_id = user_id;
        this.authorities = authorities;
        this.tenantId = tenantId;
    }

    public long getUser_id(){
        return user_id;
    }

    public String getTenantId() {return tenantId;}
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
