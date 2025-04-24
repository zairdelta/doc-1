package com.woow.axsalud.service.api;

import com.woow.axsalud.service.api.dto.ConsultationMessage;
import com.woow.axsalud.service.api.dto.ConsultationDTO;
import com.woow.axsalud.service.api.dto.SymptomsDTO;
import com.woow.axsalud.service.api.exception.ConsultationServiceException;
import com.woow.core.service.api.exception.WooUserServiceException;
import org.springframework.web.multipart.MultipartFile;

public interface ConsultationService {
    void handledConsultationMessage(ConsultationMessage consultationMessage);
    ConsultationDTO create(SymptomsDTO symptomsDTO, String userName) throws WooUserServiceException;
    void validate(String consultationId,
                  String receiver, String sender) throws ConsultationServiceException;
    ConsultationDTO assign(String doctor, String consultationId) throws ConsultationServiceException;
    void addMessage(ConsultationMessage consultationMessage)
            throws ConsultationServiceException;

    String appendDocument(String userName, String consultationId, MultipartFile file) throws ConsultationServiceException;
    String downloadDocument(String userName, String consultationId, long fileId) throws ConsultationServiceException;
}
