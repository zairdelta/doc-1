package com.woow.storage.api;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    StorageServiceUploadResponseDTO uploadFile(MultipartFile file)
            throws StorageServiceException;
    String generateSignedUrl(String publicId, int expirationInSeconds)
            throws StorageServiceException;
}
