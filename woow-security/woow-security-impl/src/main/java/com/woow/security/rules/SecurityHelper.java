package com.woow.security.rules;

import com.woow.security.api.exception.WooSecurityException;
import com.woow.security.api.WooSecurityUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SecurityHelper {

    public ResponseEntity verify(final int http_request_user_id) throws WooSecurityException {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        long token_user_id = -1;

        if (principal instanceof WooSecurityUserDetails) {
            token_user_id = ((WooSecurityUserDetails)principal).getUser_id();
        } else {
            throw new WooSecurityException("bad request, bad principal", 401);
        }

        if (token_user_id != http_request_user_id) {
            throw new WooSecurityException("forbidden", 401);
        }

        return ResponseEntity.ok().build();
    }

    public ResponseEntity verify(final String http_request_user_name) throws WooSecurityException {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String userName = "";

        if (principal instanceof WooSecurityUserDetails) {
            userName = ((WooSecurityUserDetails)principal).getUsername();
        } else {
            throw new WooSecurityException("bad request, bad principal", 401);
        }

        if (Objects.isNull(userName)) {
            throw new WooSecurityException("forbidden", 401);
        }

        if (!userName.equalsIgnoreCase(http_request_user_name)) {
            throw new WooSecurityException("forbidden", 401);
        }

        return ResponseEntity.ok().build();
    }

}