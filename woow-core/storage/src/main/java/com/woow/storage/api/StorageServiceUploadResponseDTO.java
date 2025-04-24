package com.woow.storage.api;

import lombok.Data;

@Data
public class StorageServiceUploadResponseDTO {
    private String publicId;
    private String originalFilename;
    private String secureUrl;
    private String format;
    private String resourceType;
    private String fileType;
    private long bytes;
    private String createdAt;
}
