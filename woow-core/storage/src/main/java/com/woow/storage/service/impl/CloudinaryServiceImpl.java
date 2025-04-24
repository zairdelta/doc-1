package com.woow.storage.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.woow.storage.api.StorageService;
import com.woow.storage.api.StorageServiceException;
import com.woow.storage.api.StorageServiceUploadResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CloudinaryServiceImpl implements StorageService {

    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public StorageServiceUploadResponseDTO uploadFile(MultipartFile file) throws StorageServiceException {
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "auto",
                            "use_filename", true,
                            "unique_filename", true,
                            "secure", true
                    )
            );

            StorageServiceUploadResponseDTO result = new StorageServiceUploadResponseDTO();
            result.setPublicId((String) uploadResult.get("public_id"));
            result.setOriginalFilename((String) uploadResult.get("original_filename"));
            result.setSecureUrl((String) uploadResult.get("secure_url"));
            result.setFormat((String) uploadResult.get("format"));
            result.setResourceType((String) uploadResult.get("resource_type"));
            result.setFileType(result.getResourceType() + "/" + result.getFormat());

            Object bytesValue = uploadResult.get("bytes");
            if (bytesValue instanceof Number) {
                result.setBytes(((Number) bytesValue).longValue());
            }

            result.setCreatedAt((String) uploadResult.get("created_at"));
            log.info("Result storage: {}", result);
            return result;

        } catch (IOException e) {
            log.error("Failed to upload file {}: {}", file.getOriginalFilename(), e.getMessage());
            throw new StorageServiceException("Failed to upload file to Storage Service", 500);
        }
    }

    @Override
    public String generateSignedUrl(String publicId, int expirationInSeconds)
            throws StorageServiceException {
        try {
            long expirationTimestamp = System.currentTimeMillis() / 1000 + expirationInSeconds;

            Map<String, Object> params = ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", "auto",
                    "type", "authenticated",
                    "timestamp", expirationTimestamp
            );

            String signature = cloudinary.apiSignRequest(params, cloudinary.config.apiSecret);

            String url = String.format(
                    "https://res.cloudinary.com/%s/%s/%s/v1/%s?timestamp=%s&signature=%s&api_key=%s",
                    cloudinary.config.cloudName,
                    "auto",
                    "authenticated",
                    publicId,
                    expirationTimestamp,
                    signature,
                    cloudinary.config.apiKey
            );

            return url;

        } catch (Exception e) {
            log.error("Error generating secureURL: " + e.getMessage());
            throw new StorageServiceException("Failed to generate signed URL", 402);
        }
    }

}
