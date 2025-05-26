package com.woow.security.api;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

@Component
@Slf4j
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = -2550185165626007488L;

    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;
    public static final long JWT_TOKEN_VALIDITY_ONE_HOUR = 1 * 60 * 60;

    private static final String TENANT_ID = "X-TenantID";
    private static final String ENTERPRISE = "enterprise";
    private static final String USER_ID = "userId";
    private static final String ROLES = "roles";

    @Value("${JWT_SECRET:U3Xkb/nQsVknKo7Gjf+aFndO9uZEd07VhIyLBUPeINNX04Q54tqlueoxnafvUDGHBntLrynOCFF6PKPDkHmWow==")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public String getTenantId(String token) {
       // Claims claims = getAllClaimsFromToken(token);
       // return claims.get(TENANT_ID) == null ? defaultTenant : (String) claims.get(TENANT_ID);
        return null;
    }

    public int getUserId(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get(USER_ID) == null ? 0 : (Integer) claims.get(USER_ID);
    }

    public String getEnterprise(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get(ENTERPRISE) == null ? "" : (String) claims.get(ENTERPRISE);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public Claims getAllClaimsFromToken(String token) {
        Jwt<?, ?> jwt = Jwts.parser().verifyWith(getSigningKey()).build().parse(token);
        return (Claims) jwt.getPayload();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(String tenantId, long userId, UserDetails userDetails, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TENANT_ID, tenantId);
        claims.put(USER_ID, userId);
        claims.put(ROLES, roles);
        return doGenerateToken(claims, userDetails.getUsername());
    }

    public List<String> getRoles(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get(ROLES) == null ? new ArrayList<>() : (List<String>) claims.get(ROLES);
    }

    public String generateAnonymousToken(String tenantId, int userId, String userName, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TENANT_ID, tenantId);
        claims.put(USER_ID, userId);
        claims.put(ROLES, roles);
        return doGenerateTokenAnonymous(claims, userName);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername());
    }

    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    private String doGenerateTokenAnonymous(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY_ONE_HOUR * 1000))
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
