package com.woow.security.config.filters;

import com.woow.axsalud.common.multitenant.EnterpriseContext;
import com.woow.axsalud.common.multitenant.TenantContext;
import com.woow.axsalud.common.multitenant.UserContext;
import com.woow.axsalud.common.multitenant.UserInformation;
import com.woow.security.BlackListServiceImpl;
import com.woow.security.api.JwtTokenUtil;
import com.woow.security.api.JwtUserDetailsService;
import com.woow.security.api.WooSecurityUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private static String MALICIOUS_IP = "103.14.26.88";

    private static String HEALTH_INSURANCE_USER_NAME = "health_insurance";
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private BlackListServiceImpl blackListService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {


        String clientIp = getClientIP(request);

        if(MALICIOUS_IP.equalsIgnoreCase(clientIp)) {
            log.warn("ip is malicious: {}, clientID: {}", MALICIOUS_IP.equalsIgnoreCase(clientIp), clientIp);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        int userId = 0;

        log.info("Validating token");
        String jwtToken = null;
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            log.info("Token created, Bearer Format");
        } else {

            String query = request.getQueryString();
            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("token=")) {
                        jwtToken = param.substring("token=".length());
                        log.info("Token created from uri as token");
                        break;
                    }
                    if (param.startsWith("tokenBearer=")) {
                        jwtToken = param.substring("tokenBearer=".length());
                        log.info("Token created from uri as tokenBearer");
                        break;
                    }
                }
            }

            if (jwtToken != null) {
                log.info("JWT Token extracted from URL query string or header");
            } else {
                log.warn("JWT Token not found in Authorization header or URL, ip: {}", getClientIP(request));
            }
        }


        UserInformation userInformation = new UserInformation();

        String requestUri = request.getRequestURI();

        if (requestUri.startsWith("/api/tm/")) {
            String tenantIdFromHid = extractHidFromUri(requestUri);
            TenantContext.setCurrentTenant(tenantIdFromHid);
        } else {

            // JWT Token is in the form "Bearer token". Remove Bearer word and get
            // only the Token
          //  if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {

                //jwtToken = requestTokenHeader.substring(7);
                try {
                    username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                    //TenantContext.setCurrentTenant(jwtTokenUtil.getTenantId(jwtToken));
                    EnterpriseContext.setCurrentEnterprise(jwtTokenUtil.getEnterprise(jwtToken));
                    userInformation.setUserName(username);
                    userId = jwtTokenUtil.getUserId(jwtToken);
                    userInformation.setUserId(userId);
                    UserContext.setCurrentUserInformation(userInformation);
                } catch (IllegalArgumentException e) {
                    log.error("Unable to get JWT Token");
                } catch (final ExpiredJwtException e) {
                    log.error("JWT Token has expired");
                }
           // } else {
           //     log.warn("JWT Token does not begin with Bearer String, ip: {}",
          //          getClientIP(request));
          //  }

            // Once we get the token validate it.
            if (username != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {

                try {
                    UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(
                        username);
                    // if token is valid configure Spring Security to manually set
                    // authentication
                    if (!blackListService.isTokenInBl(jwtToken) &&
                        HEALTH_INSURANCE_USER_NAME.equalsIgnoreCase(username)) {
                        setSecurityContext(userDetails, request);
                    } else {
                        // if token is valid configure Spring Security to manually set
                        // authentication
                        if (!blackListService.isTokenInBl(jwtToken) && jwtTokenUtil.validateToken(
                            jwtToken,
                            userDetails)) {
                            setSecurityContext(userDetails, request);
                        }
                    }
                } catch (UsernameNotFoundException une) {
                    if (!blackListService.isTokenInBl(jwtToken) && !ObjectUtils.isEmpty(jwtToken)) {
                        Claims claims = jwtTokenUtil.getAllClaimsFromToken(jwtToken);
                        List roles = (List<String>) claims.get("roles");

                       // TenantContext.setCurrentTenant(jwtTokenUtil.getTenantId(jwtToken));
                        EnterpriseContext.setCurrentEnterprise(
                            jwtTokenUtil.getEnterprise(jwtToken));
                        userInformation.setUserName(username);
                        userId = jwtTokenUtil.getUserId(jwtToken);
                        userInformation.setUserId(userId);
                        UserContext.setCurrentUserInformation(userInformation);

                        UserDetails userDetails = new
                            WooSecurityUserDetails((Integer) claims.get("userId"),
                            (String) claims.get("sub"), "NO_PWD",
                            mapToGrantedAuthorities(roles), (String) claims.get("X-TenantID"));
                        setSecurityContext(userDetails, request);
                    }
                }


            } else {
                if (!blackListService.isTokenInBl(jwtToken) && !ObjectUtils.isEmpty(jwtToken)) {
                    Claims claims = jwtTokenUtil.getAllClaimsFromToken(jwtToken);
                    List roles = (List<String>) claims.get("roles");
                 //   TenantContext.setCurrentTenant(jwtTokenUtil.getTenantId(jwtToken));
                    EnterpriseContext.setCurrentEnterprise(jwtTokenUtil.getEnterprise(jwtToken));
                    userInformation.setUserName(username);
                    userId = jwtTokenUtil.getUserId(jwtToken);
                    userInformation.setUserId(userId);
                    UserContext.setCurrentUserInformation(userInformation);

                    UserDetails userDetails = new
                        WooSecurityUserDetails(Integer.valueOf((String) claims.get("userId")),
                        (String) claims.get("sub"), "NO_PWD",
                        mapToGrantedAuthorities(roles), (String) claims.get("X-TenantID"));
                    setSecurityContext(userDetails, request);
                }
            }
        }
        chain.doFilter(request, response);
    }

    private static List<GrantedAuthority> mapToGrantedAuthorities(List<String> roles) {
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority(role))
            .collect(Collectors.toList());
    }

    private void setSecurityContext(UserDetails userDetails, HttpServletRequest request) {

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
        usernamePasswordAuthenticationToken
            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        // After setting the Authentication in the context, we specify
        // that the current user is authenticated. So it passes the
        // Spring Security Configurations successfully.
        SecurityContextHolder.getContext()
            .setAuthentication(usernamePasswordAuthenticationToken);
    }
    private String getClientIP(HttpServletRequest request) {
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private String extractHidFromUri(String uri) {
        Pattern pattern = Pattern.compile("/api/tm/([^/]+)-");
        Matcher matcher = pattern.matcher(uri);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

}