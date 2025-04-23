package com.woow.security.jwt;

import com.woow.security.api.JwtTokenUtil;
import com.woow.security.api.WooBoTokenService;
import com.woow.security.api.WooSecurityUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WooBoTokenServiceImpl implements WooBoTokenService {

    @Autowired
    private JwtTokenUtil tokenUtil;

    public String createTokenForUserId(final String userName) {
        WooSecurityUserDetails userDetails = new WooSecurityUserDetails(userName);
        return tokenUtil.generateToken(userDetails);
    }

    public boolean validateToken(final String userName, final String token) {
        WooSecurityUserDetails userDetails = new WooSecurityUserDetails(userName);
        return tokenUtil.validateToken(token, userDetails);
    }

}
