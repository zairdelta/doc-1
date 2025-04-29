package com.woow.storage.service.impl;

import com.cloudinary.AuthToken;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.Url;
import com.cloudinary.utils.ObjectUtils;
import com.woow.storage.api.StorageService;
import com.woow.storage.api.StorageServiceException;
import com.woow.storage.api.StorageServiceUploadResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

            String originalFilename = file.getOriginalFilename();
            String fallbackFormat = null;

            if (originalFilename != null && originalFilename.contains(".")) {
                fallbackFormat = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
            }

            Map<String, Object> tokenAccess = new HashMap<>();
            tokenAccess.put("access_type", "token");

            List<Map<String, Object>> accessControlList = new ArrayList<>();
            accessControlList.add(tokenAccess);

            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "use_filename", true,
                            "unique_filename", true,
                           "secure", true, // false for now
                           //TODO we need premium to add "secure", true,
                            //TODO we need premium to add "access_mode", "authenticated",
                         //TODO   "access_control", accessControlList,
                            "format", fallbackFormat
                    )
            );

            StorageServiceUploadResponseDTO result = new StorageServiceUploadResponseDTO();
            result.setPublicId((String) uploadResult.get("public_id"));
            result.setOriginalFilename((String) uploadResult.get("original_filename"));
            result.setSecureUrl((String) uploadResult.get("secure_url"));
            result.setFormat(fallbackFormat);
            Object versionObj = uploadResult.get("version");
            if (versionObj != null) {
                result.setVersion(versionObj.toString()); // assuming your DTO has a 'version' field as String
            }

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
    public String generateSignedUrl(String publicId, String format,
                                    String version, int expirationInSeconds)
            throws StorageServiceException {
        try {


//TODO we need premium to access this feature
            String signedUrl = cloudinary.url().transformation(new Transformation())
                    .type("authenticated")
                    .authToken(new AuthToken(cloudinary.config.apiKey)
                    .duration(300))
                    .signed(true)
                    .publicId(publicId)
                    .generate();

            return signedUrl;
        } catch (Exception e) {
            log.error("Error generating secureURL: " + e.getMessage());
            throw new StorageServiceException("Failed to generate signed URL", 402);
        }
    }

}
