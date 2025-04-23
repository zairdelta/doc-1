package com.woow.security;

import com.woow.security.api.BlackListRepository;
import com.woow.security.api.BlackListService;
import com.woow.security.api.JwtBlackList;
import com.woow.security.api.exception.JwtBlacklistException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BlackListServiceImpl implements BlackListService {

    @Autowired
    private BlackListRepository blackListRepository;

    public boolean isTokenInBl(final String token) {
        JwtBlackList blackListJwt = blackListRepository.findbyToken(token);
        return blackListJwt != null;
    }

    public void addEntry(final String token) throws JwtBlacklistException {

        if (null == token) {
            throw new JwtBlacklistException("token cannot be null in black list service, as parameter");
        }

        JwtBlackList jwtBlackList = new JwtBlackList();
        jwtBlackList.setToken(token);
        blackListRepository.save(jwtBlackList);
    }

    public void deleteEntry(final String token) {

        JwtBlackList blackListJwt = blackListRepository.findbyToken(token);

        if (blackListJwt != null) {
            blackListRepository.delete(blackListJwt);
        }
    }
}
