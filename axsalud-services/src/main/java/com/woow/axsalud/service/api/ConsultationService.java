package com.woow.axsalud.service.api;

import com.woow.axsalud.data.consultation.ConsultationSession;
import com.woow.axsalud.data.consultation.ConsultationStatus;
import com.woow.axsalud.data.consultation.DoctorPrescription;
import com.woow.axsalud.data.consultation.LaboratoryPrescription;
import com.woow.axsalud.service.api.dto.*;
import com.woow.axsalud.service.api.exception.ConsultationServiceException;
import com.woow.core.service.api.exception.WooUserServiceException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ConsultationService {
    void handledConsultationMessage(ConsultationMessageDTO consultationMessage);
    ConsultationDTO create(SymptomsDTO symptomsDTO, String userName) throws WooUserServiceException;

    ConsultationDTO continueWithConsultation(String userName, String consultationId)
            throws WooUserServiceException;
    void validate(String consultationId, String consultationSessionId,
                  String receiver, String sender) throws ConsultationServiceException;
    ConsultationDTO assign(String doctor, String consultationId, String consultationSessionId) throws ConsultationServiceException;
    void addMessage(ConsultationMessageDTO consultationMessage)
            throws ConsultationServiceException;

    FileResponseDTO appendDocument(String userName, String consultationSessionId, MultipartFile file) throws ConsultationServiceException;
    FileResponseDTO downloadDocument(String userName, String consultationSessionId, long fileId) throws ConsultationServiceException;
    ConsultationDTO getbyConsultationId(String userName, String consultationId);
    List<ConsultationDTO> getConsultationsByStatus(ConsultationStatus status);
    ConsultationMessagesPagingDTO getAllMessageByUserNameUsingPaginationPagination
            (String userName, int pageNumber, int totalElementsPerPage) throws ConsultationServiceException;

    ConsultationMessagesPagingDTO getAllMessagesGivenConsultationIdAndSessionId(String consultationId,
                                                                                String consultationSessionId,
                                                                                int pageNumber,
                                                                                int totalElementsPerPage)
            throws ConsultationServiceException;

    ConsultationSession getConsultationSession(String consultationSessionId) throws ConsultationServiceException;
    void closeSession(String consultationId, String consultationSessionId, String sender)
            throws ConsultationServiceException;

    ConsultationSessionViewDTO getConsultationSession(String userName, String consultationSessionId) throws ConsultationServiceException;

    void addDoctorPrescriptions(String userName, String consultationId, String consultationSessionId, List<DoctorPrescription> doctorPrescriptions) throws ConsultationServiceException;
    void addLaboratoryPrescriptions(String userName, String consultationId, String consultationSessionId, List<LaboratoryPrescription> laboratoryPrescriptions) throws ConsultationServiceException;

}
