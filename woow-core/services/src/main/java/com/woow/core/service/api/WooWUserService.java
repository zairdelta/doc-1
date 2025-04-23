package com.woow.core.service.api;

import com.woow.core.service.api.exception.WooUserServiceException;

public interface WooWUserService {
    String save(UserDtoCreate wooUserDTO) throws WooUserServiceException;
    void addRoleToUser(long userId, String role) throws WooUserServiceException;

    WooUserViewDTO getUserByName(String userName) throws WooUserServiceException;
    WooUserViewDTO getByUserId(long userId) throws WooUserServiceException;
    void updateWooUserByUserName(String userName, UserUpdateDto wooUserDTO) throws WooUserServiceException;
    void updateWooUserByUserId(long userId, UserUpdateDto wooUserDTO) throws WooUserServiceException;

    void cancelMembership(long userId) throws WooUserServiceException;
    void email_confirmed(final String userName, boolean isEmailConfirmed) throws WooUserServiceException;
    void updateCredentials(String userName, UserCredentialsDto userCredentialsDto) throws WooUserServiceException;


}
