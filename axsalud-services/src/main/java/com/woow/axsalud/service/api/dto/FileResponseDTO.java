package com.woow.axsalud.service.api.dto;

import lombok.Data;

@Data
public class FileResponseDTO {
    private String name;
    private String url;
    private String consultationId;
    private String consultationSessionId;
    private long id;
}
