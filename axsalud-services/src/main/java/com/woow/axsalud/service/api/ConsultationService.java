package com.woow.axsalud.service.api;

import com.woow.axsalud.service.api.dto.ConsultationMessage;
import com.woow.axsalud.service.api.dto.ConsultationDTO;
import com.woow.axsalud.service.api.dto.SymptomsDTO;
import com.woow.axsalud.service.api.exception.ConsultationServiceException;
import com.woow.core.service.api.exception.WooUserServiceException;

public interface ConsultationService {
    void handledConsultationMessage(ConsultationMessage consultationMessage);
    ConsultationDTO create(SymptomsDTO symptomsDTO, String userName) throws WooUserServiceException;
    void validate(String consultationId,
                  String receiver, String sender) throws ConsultationServiceException;
    ConsultationDTO assign(String doctor, String consultationId) throws ConsultationServiceException;
    void addMessage(ConsultationMessage consultationMessage)
            throws ConsultationServiceException;
}
