package com.woow.axsalud.controller;

import com.woow.axsalud.controller.exception.WooBoHttpError;
import com.woow.core.data.repository.WoowUserRepository;
import com.woow.core.data.user.WoowUser;
import com.woow.security.api.*;
import com.woow.security.api.exception.InvalidReCaptchaException;
import com.woow.security.api.exception.JwtBlacklistException;
import com.woow.security.api.exception.ReCaptchaInvalidException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Api(tags = "Authentication service")
@Validated
public class WooBoUserAuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    private WoowUserRepository userRepository;

   // @Autowired
   // private SMSNotificationService smsNotificationService;

    @Autowired
    private BlackListService blackListService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value( "${user.login.retries:3}" )
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK - authentication was successfully.", response = JwtResponse.class),
            @ApiResponse(code = 401, message = "no authenticated")
    })
    @ApiOperation(value = "authenticate the user before accessing to the system.")
    @CrossOrigin(origins = "*")
    public ResponseEntity ms_login(@RequestBody JwtRequest authenticationRequest) {

        WoowUser wooUser = null;
        boolean isAnonymousUserEnabled =
            secret.equals(authenticationRequest.getPassword()) ||
                secret2.equals(authenticationRequest.getPassword());

        log.info("isAnonymousUserEnabled: ", isAnonymousUserEnabled);

        try {

            if (null == authenticationRequest.getUsername()) {
                WooBoHttpError wooBoHttpError = new WooBoHttpError();
                wooBoHttpError.setMessage("wrong username or password.");
                wooBoHttpError.setCode(401);
                return ResponseEntity.status(401).body(wooBoHttpError);
            }

            if ("".equals(authenticationRequest.getUsername())) {
                WooBoHttpError wooBoHttpError = new WooBoHttpError();
                wooBoHttpError.setMessage("wrong username or password.");
                wooBoHttpError.setCode(401);
                return ResponseEntity.status(401).body(wooBoHttpError);
            }

            wooUser = userRepository.findByUserName(authenticationRequest.getUsername());

            if (wooUser == null) {
                WooBoHttpError wooBoHttpError = new WooBoHttpError();
                wooBoHttpError.setMessage("wrong username or password.");
                wooBoHttpError.setCode(401);

                return ResponseEntity.status(401).body(wooBoHttpError);
            }

            if (!isAnonymousUserEnabled && wooUser.getIs_user_blocked() == 1) {
                WooBoHttpError wooBoHttpError = new WooBoHttpError();
                wooBoHttpError.setMessage("User is blocked by the system due max retries reached." +
                    "  Use Recovery link.");
                wooBoHttpError.setCode(401);

                return ResponseEntity.status(401).body(wooBoHttpError);
            }

            /*if (wooUser.getUserName() != null &&
                (wooUser.getUserName().equalsIgnoreCase("user1") ||
                 wooUser.getUserName().equalsIgnoreCase("user1"))) {
                WooBoHttpError wooBoHttpError = new WooBoHttpError();
                wooBoHttpError.setMessage("wrong username or password.");
                wooBoHttpError.setCode(401);
                return ResponseEntity.status(401).body(wooBoHttpError);
            }*/


            if (!isAnonymousUserEnabled) {
                authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
            }


        } catch (final ReCaptchaInvalidException e) {
            log.error("Password resetting failed, reason being " + e.getMessage());
            WooBoHttpError wooBoHttpError = WooBoHttpError.of(e);
            return wooBoHttpError.toResponseEntity();
        } catch (InvalidReCaptchaException e) {
            log.error("Password resetting failed, reason being " + e.getMessage());
            WooBoHttpError wooBoHttpError = WooBoHttpError.of(e);
            return wooBoHttpError.toResponseEntity();
        } catch (Exception e) {
            log.error("Exception happen while login: {}", e);
            WooBoHttpError wooBoHttpError = new WooBoHttpError();
            wooBoHttpError.setMessage("wrong username or password.");
            wooBoHttpError.setCode(401);

            if(wooUser != null) {
                int login_attemps = wooUser.getLogin_attempts();
                login_attemps -= 1;

                if (login_attemps == 0) {
                    wooUser.setIs_user_blocked(1);
                    wooBoHttpError.setMessage("User is blocked by the system due max retries reached." +
                        "  Use Recovery link.");
                } else {
                    wooUser.setLogin_attempts(login_attemps);
                    wooBoHttpError.setMessage("wrong username or password." );
                }
                userRepository.save(wooUser);

            }

            return ResponseEntity.status(401).body(wooBoHttpError);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

        if (!(userDetails instanceof WooSecurityUserDetails)) {
            WooBoHttpError wooBoHttpError = new WooBoHttpError();
            wooBoHttpError.setMessage("wrong username or password.");
            wooBoHttpError.setCode(401);

            if(wooUser != null) {
                int login_attemps = wooUser.getLogin_attempts() - 1;
                wooUser.setLogin_attempts(login_attemps);
                userRepository.save(wooUser);
                wooBoHttpError.setMessage("wrong username or password. ");
            }

            return ResponseEntity.status(401).body(wooBoHttpError);
        }

        if (!isAnonymousUserEnabled && wooUser.getPhoneNumberConfirm()) {
            /*try {
                smsNotificationService.sms_authentication(authenticationRequest.getUsername());
            } catch (SMSTwoFactorAuthenticationException e) {
                WooBoHttpError wooBoHttpError = new WooBoHttpError();
                wooBoHttpError.setMessage("MFA is not available at the moment, we are working to fix the issue," +
                    " if problem persists  Use Recovery link.");
                wooBoHttpError.setCode(401);
            } */
            wooUser.setMfa(1);
            userRepository.save(wooUser);
            return ResponseEntity.status(207).build();
        }

        wooUser.setMfa(0);

        final String token = jwtTokenUtil.generateToken(wooUser.getTenantId(),
            wooUser.getUserId(),
            userDetails, wooUser.getSecurityRoles());

        WooSecurityUserDetails wooUserDetails = (WooSecurityUserDetails)userDetails;
        JwtResponse jwtResponse = new JwtResponse(token, wooUserDetails.getUser_id());
        wooUser.setLogin_attempts(retries);
        userRepository.save(wooUser);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping(path = "/user_logout")
    public ResponseEntity logout(@RequestBody TokenDTO tokenDto) {

        if(tokenDto == null) {
            return ResponseEntity.ok().build();
        }

        if(ObjectUtils.isEmpty(tokenDto.getToken())) {
            return ResponseEntity.ok().build();
        }

        try {
            blackListService.addEntry(tokenDto.getToken());
        } catch (final JwtBlacklistException e) {
            log.error("token could not be added to the black list");
        }

        return ResponseEntity.ok().build();
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

}
