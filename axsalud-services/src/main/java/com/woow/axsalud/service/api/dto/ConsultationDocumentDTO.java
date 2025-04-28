package com.woow.axsalud.service.api.dto;

import com.woow.axsalud.data.client.AxSaludWooUser;
import com.woow.axsalud.data.consultation.ConsultationDocument;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsultationDocumentDTO {
    private Long id;
    private String fileName;
    private String fileType;
    private String location;
    private String format;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime lastModifiedAt;
    private String version;
    private AxSaludUserViewDTO uploadedBy;

    public static ConsultationDocumentDTO from(ConsultationDocument consultationDocument) {
        ConsultationDocumentDTO consultationDocumentDTO = new ConsultationDocumentDTO();
        consultationDocumentDTO.setId(consultationDocument.getId());
        consultationDocumentDTO.setFileName(consultationDocument.getFileName());
        consultationDocumentDTO.setFileType(consultationDocumentDTO.getFileType());
        consultationDocumentDTO.setLocation(consultationDocument.getSecureUrl());
        return consultationDocumentDTO;

    }
}
