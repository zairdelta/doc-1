package com.woow.security.api;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class JwtRequest {
    private String username;
    private String password;
}
