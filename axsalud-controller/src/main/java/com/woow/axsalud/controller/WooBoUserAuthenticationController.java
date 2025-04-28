package com.woow.axsalud.controller;

import com.woow.axsalud.controller.exception.WooBoHttpError;
import com.woow.core.data.repository.WoowUserRepository;
import com.woow.core.data.user.WoowUser;
import com.woow.security.api.*;
import com.woow.security.api.exception.InvalidReCaptchaException;
import com.woow.security.api.exception.JwtBlacklistException;
import com.woow.security.api.exception.ReCaptchaInvalidException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/woo_user/authenticate")
@CrossOrigin(origins = "*")
@Slf4j
@Validated
@Tag(name = "Authentication service", description = "Handles login, token generation, and logout")
public class WooBoUserAuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    private WoowUserRepository userRepository;

    @Autowired
    private BlackListService blackListService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${user.login.retries:3}")
    private int retries;

    @Value("${woo.bo.payment.user:woo_bo_user_payment_user}")
    private String ms_user;

    @Value("${woo.bo.user.anonymous.secret:invalid_secret}")
    private String secret;

    @Value("${woo.bo.user.anonymous.secret2:invalid_secret}")
    private String secret2;

    @Autowired
    private ICaptchaService captchaService;

    @PostMapping(path = "ms_login")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Authenticate user", description = "Authenticate the user before accessing the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<?> ms_login(@RequestBody JwtRequest authenticationRequest) {
        WoowUser wooUser = null;
        boolean isAnonymousUserEnabled =
                secret.equals(authenticationRequest.getPassword()) ||
                        secret2.equals(authenticationRequest.getPassword());

        log.info("isAnonymousUserEnabled: {}", isAnonymousUserEnabled);

        try {
            if (authenticationRequest.getUsername() == null || authenticationRequest.getUsername().isEmpty()) {
                return errorResponse("wrong username or password.", 401);
            }

            wooUser = userRepository.findByUserName(authenticationRequest.getUsername());

            if (wooUser == null) {
                return errorResponse("wrong username or password.", 401);
            }

            if (!isAnonymousUserEnabled && wooUser.getIs_user_blocked() == 1) {
                return errorResponse("User is blocked due to max retries. Use Recovery link.", 401);
            }

            if (!isAnonymousUserEnabled) {
                authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
            }

        } catch (Exception e) {
            log.error("Login exception: {}", e.getMessage(), e);
            return handleLoginError(e, wooUser);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

        if (!(userDetails instanceof WooSecurityUserDetails)) {
            return handleLoginError(null, wooUser);
        }

        if (!isAnonymousUserEnabled && wooUser.getPhoneNumberConfirm()) {
            wooUser.setMfa(1);
            userRepository.save(wooUser);
            return ResponseEntity.status(207).build(); // Multi-Status to indicate MFA required
        }

        wooUser.setMfa(0);

        final String token = jwtTokenUtil.generateToken(
                wooUser.getTenantId(),
                wooUser.getUserId(),
                userDetails,
                wooUser.getSecurityRoles()
        );

        WooSecurityUserDetails wooUserDetails = (WooSecurityUserDetails) userDetails;
        JwtResponse jwtResponse = new JwtResponse(token, wooUserDetails.getUser_id());
        wooUser.setLogin_attempts(retries);
        String firstAuthority = wooUserDetails.getAuthorities()
                .iterator()
                .next()
                .getAuthority();
        jwtResponse.setAppRole(firstAuthority);
        userRepository.save(wooUser);

        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping(path = "/user_logout")
    @Operation(summary = "Log user out", description = "Invalidate a JWT by adding it to the blacklist.")
    public ResponseEntity<?> logout(@RequestBody TokenDTO tokenDto) {
        if (tokenDto == null || ObjectUtils.isEmpty(tokenDto.getToken())) {
            return ResponseEntity.ok().build();
        }

        try {
            blackListService.addEntry(tokenDto.getToken());
        } catch (final JwtBlacklistException e) {
            log.error("Token could not be blacklisted: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok().build();
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

    private ResponseEntity<?> errorResponse(String message, int code) {
        WooBoHttpError error = new WooBoHttpError();
        error.setMessage(message);
        error.setCode(code);
        return ResponseEntity.status(code).body(error);
    }

    private ResponseEntity<?> handleLoginError(Exception e, WoowUser wooUser) {
        WooBoHttpError error = new WooBoHttpError();
        error.setMessage("wrong username or password.");
        error.setCode(401);

        if (wooUser != null) {
            int loginAttempts = wooUser.getLogin_attempts() - 1;
            if (loginAttempts <= 0) {
                wooUser.setIs_user_blocked(1);
                error.setMessage("User is blocked by the system due max retries reached. Use Recovery link.");
            } else {
                wooUser.setLogin_attempts(loginAttempts);
            }
            userRepository.save(wooUser);
        }

        return ResponseEntity.status(401).body(error);
    }
}
