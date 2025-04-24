package com.woow.storage.api;

import com.woow.core.service.api.exception.WooBoException;
public class StorageServiceException extends WooBoException{
    public StorageServiceException(String msg, int code) {
        super(msg, code);
    }
}
