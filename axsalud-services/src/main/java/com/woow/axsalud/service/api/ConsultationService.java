package com.woow.axsalud.service.api;

import com.woow.axsalud.service.api.dto.ConsultationMessageDTO;
import com.woow.axsalud.service.api.dto.ConsultationDTO;
import com.woow.axsalud.service.api.dto.SymptomsDTO;
import com.woow.axsalud.service.api.exception.ConsultationServiceException;
import com.woow.core.service.api.exception.WooUserServiceException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ConsultationService {
    void handledConsultationMessage(ConsultationMessageDTO consultationMessage);
    ConsultationDTO create(SymptomsDTO symptomsDTO, String userName) throws WooUserServiceException;
    void validate(String consultationId, String consultationSessionId,
                  String receiver, String sender) throws ConsultationServiceException;
    ConsultationDTO assign(String doctor, String consultationId, String consultationSessionId) throws ConsultationServiceException;
    void addMessage(ConsultationMessageDTO consultationMessage)
            throws ConsultationServiceException;

    long appendDocument(String userName, String consultationSessionId, MultipartFile file) throws ConsultationServiceException;
    String downloadDocument(String userName, String consultationSessionId, long fileId) throws ConsultationServiceException;

    List<ConsultationDTO> getAllConsultation();
}
