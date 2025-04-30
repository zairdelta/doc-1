package com.woow.core.service.impl;

import com.woow.core.data.repository.WoowUserRepository;
import com.woow.core.data.user.WoowUser;
import com.woow.core.service.api.*;
import com.woow.core.service.api.exception.WooUserServiceException;
import com.woow.security.api.SecurityService;
import com.woow.security.api.SecurityUser;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
@Transactional
@Slf4j
public class WooWUserServiceImpl implements WooWUserService, SecurityService {

    private WoowUserRepository woowUserRepository;
    private PasswordEncoder passwordEncoder;
    private ModelMapper modelMapper;


    public WooWUserServiceImpl(WoowUserRepository woowUserRepository,
                               @Lazy PasswordEncoder passwordEncoder,
                               ModelMapper modelMapper) {
        this.woowUserRepository = woowUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    @Override
    public String save(UserDtoCreate wooUserDTO) throws WooUserServiceException {
        if (ObjectUtils.isEmpty(wooUserDTO.getPassword())) {
            throw new WooUserServiceException("Password cannot be empty", 402);
        }


        WoowUser isUserPresent = woowUserRepository.findByEmail(wooUserDTO.getEmail());

        if (isUserPresent != null) {
            throw new WooUserServiceException("user already exists with email: " +
                    wooUserDTO.getEmail(), 411);
        }

        WoowUser wooUser = new WoowUser();

        log.info("wooUserDTO {}", wooUserDTO);
        modelMapper.map(wooUserDTO, wooUser);
        wooUser.setUserId(0);
        wooUser.setPassword(passwordEncoder.encode(wooUserDTO.getPassword()));

        wooUser = woowUserRepository.save(wooUser);


        log.info("new woo user after save: {} ", wooUser);

        return wooUser.getUserName();
    }

    @Override
    public void addRoleToUser(long userId, String role) throws WooUserServiceException {
        WoowUser woowUser = woowUserRepository.findByUserId(userId);

        if(woowUser == null) {
            throw new WooUserServiceException("not found: " + userId, 404);
        }
        woowUser.getRoles().add(role);
        woowUserRepository.save(woowUser);
    }

    @Override
    public WooUserViewDTO getUserByName(String userName) throws WooUserServiceException {
        WoowUser wooUserByName = woowUserRepository.findByUserName(userName);
        if (ObjectUtils.isEmpty(wooUserByName)) {
            throw new WooUserServiceException("not found" + userName, 404);
        } else {
            WooUserViewDTO dto = new WooUserViewDTO();
            modelMapper.map(dto, wooUserByName);
            return dto;
        }
    }

    @Override
    public WooUserViewDTO getByUserId(long userId) throws WooUserServiceException {
        WoowUser woowUserByUserId = woowUserRepository.findByUserId(userId);
        if (ObjectUtils.isEmpty(woowUserByUserId)) {
            throw new WooUserServiceException("not found" + userId, 404);
        } else {
            WooUserViewDTO dto = new WooUserViewDTO();
            modelMapper.map(dto, woowUserByUserId);
            return dto;
        }
    }

    @Override
    public void updateWooUserByUserName(String userName, UserUpdateDto wooUserDTO)
            throws WooUserServiceException {
        WoowUser existingWooUser = woowUserRepository.findByUserName(userName);

        if (existingWooUser == null) {
            throw new WooUserServiceException("not found: " + userName, 404);
        }

        updateWoowUser(existingWooUser, wooUserDTO);

    }

    @Override
    public void updateWooUserByUserId(long userId, UserUpdateDto wooUserDTO) throws WooUserServiceException {
        WoowUser existingWooUser = woowUserRepository.findByUserId(userId);

        if (existingWooUser == null) {
            throw new WooUserServiceException("not found: " + userId, 404);
        }

        updateWoowUser(existingWooUser, wooUserDTO);
    }

    @Override
    public void cancelMembership(long userId) throws WooUserServiceException {

    }

    @Override
    public void email_confirmed(String userName, boolean isEmailConfirmed) throws WooUserServiceException {
        final WoowUser existingWooUser = woowUserRepository.findByUserName(userName);

        if (existingWooUser == null) {
            throw new WooUserServiceException("user does not exist." + userName,  404);
        }

        existingWooUser.setEmailConfirm(isEmailConfirmed);
        woowUserRepository.save(existingWooUser);
    }

    @Override
    public void updateCredentials(String userName, UserCredentialsDto userCredentialsDto) throws WooUserServiceException {
        final WoowUser existingWooUser = woowUserRepository.findByUserName(userName);

        if (existingWooUser == null) {
            throw new WooUserServiceException("user does not exist." + userName,  404);
        }

        existingWooUser.setPassword(passwordEncoder.encode(userCredentialsDto.getPassword()));
        //existingWooUser.setIs_user_blocked(0);
        existingWooUser.setEmailConfirm(true);
        //existingWooUser.setLogin_attempts(5);

        woowUserRepository.save(existingWooUser);
    }

    private void updateWoowUser(WoowUser existingWooUser, UserUpdateDto wooUserDTO) {

        modelMapper.map(wooUserDTO, existingWooUser);

        if (wooUserDTO.getPassword() != null &&
                !wooUserDTO.getPassword().equalsIgnoreCase("")) {
            existingWooUser.setPassword(passwordEncoder.encode(wooUserDTO.getPassword()));
        }

        if (wooUserDTO.getMobilePhone() != null &&
                !wooUserDTO.getMobilePhone().equals(existingWooUser.getMobilePhone())) {
            existingWooUser.setMobilePhone(wooUserDTO.getMobilePhone());
            existingWooUser.setPhoneNumberConfirm(false);
        }

        if (wooUserDTO.getEmail() != null &&
                !wooUserDTO.getEmail().equals(existingWooUser.getEmail())) {
            existingWooUser.setEmail(wooUserDTO.getEmail());
            existingWooUser.setEmailConfirm(false);
        }

        woowUserRepository.save(existingWooUser);

    }

    @Override
    public SecurityUser findByUserName(String username) {
        WoowUser woowUser = woowUserRepository.findByUserName(username);
        SecurityUser securityUser = new SecurityUserCore(woowUser.getUserId(),
                woowUser.getSecurityRoles(), woowUser.getUserName(), woowUser.getPassword(),
                woowUser.getTenantId());
        return securityUser;
    }
}
