package com.woow.axsalud.service.api;

import com.woow.axsalud.common.AXSaludUserRoles;
import com.woow.axsalud.data.consultation.ConsultationSession;
import com.woow.axsalud.data.consultation.ConsultationSessionStatus;
import com.woow.axsalud.data.consultation.ConsultationStatus;
import com.woow.axsalud.service.api.dto.*;
import com.woow.axsalud.service.api.exception.ConsultationServiceException;
import com.woow.axsalud.service.api.messages.ConsultationEventDTO;
import com.woow.axsalud.service.api.messages.ConsultationMessageDTO;
import com.woow.axsalud.service.api.messages.control.SessionAbandonedDTO;
import com.woow.core.service.api.exception.WooUserServiceException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ConsultationService {
    void handledConsultationMessage(String sessionId, ConsultationMessageDTO consultationMessage);
    ConsultationDTO create(SymptomsDTO symptomsDTO, String userName) throws WooUserServiceException;

    void addComentariosMedicos(DoctorCommentsDTO doctorCommentsDTO, String consultationSessionId);

    ConsultationDTO continueWithConsultation(String userName, String consultationId)
            throws WooUserServiceException;
    void validate(String consultationId, String consultationSessionId,
                  String receiver, String sender) throws ConsultationServiceException;
    ConsultationDTO assign(String doctor, String consultationId, String consultationSessionId) throws ConsultationServiceException;
    long addMessage(ConsultationMessageDTO consultationMessage, ConsultationMessgeTypeEnum consultationMessageType)
            throws ConsultationServiceException;

    FileResponseDTO appendDocument(String userName, String consultationSessionId, MultipartFile file) throws ConsultationServiceException;
    FileResponseDTO downloadDocument(String userName, String consultationSessionId, long fileId) throws ConsultationServiceException;
    ConsultationDTO getbyConsultationId(String userName, String consultationId);
    List<ConsultationDTO> getConsultationsByStatus(ConsultationStatus status);
    ConsultationMessagesPagingDTO getAllMessageByUserNameUsingPaginationPagination
            (String userName, int pageNumber, int totalElementsPerPage) throws ConsultationServiceException;

    public ConsultationMessagesPagingDTO
    getAllMessageBySessionIdUsingPaginationPagination(String sessionId, int pageNumber, int totalElementsPerPage)
            throws ConsultationServiceException;
    ConsultationMessagesPagingDTO getAllMessagesGivenConsultationIdAndSessionId(String consultationId,
                                                                                String consultationSessionId,
                                                                                int pageNumber,
                                                                                int totalElementsPerPage)
            throws ConsultationServiceException;

    ConsultationSession getConsultationSession(String consultationSessionId) throws ConsultationServiceException;
    void closeSession(String sessionId, String consultationId, String consultationSessionId, String sender)
            throws ConsultationServiceException;

    ConsultationSessionViewDTO getConsultationSession(String userName, String consultationSessionId) throws ConsultationServiceException;

    void addDoctorPrescriptions(String userName, String consultationId, String consultationSessionId, List<DoctorPrescriptionDTO> doctorPrescriptionDTOS) throws ConsultationServiceException;
    void addLaboratoryPrescriptions(String userName, String consultationId, String consultationSessionId, List<LaboratoryPrescriptionDTO> laboratoryPrescriptions) throws ConsultationServiceException;

    void consultationDisconnect(String sessionId, String consultationId,
                                String consultationSessionId, String userName, String role);
    ConsultationEventDTO<SessionAbandonedDTO>
    handledSessionAbandoned(final String transportSessionId, final ConsultationSession consultationSession,
                            ConsultationSessionStatus status,
                            final AXSaludUserRoles role, String userName)

    String sendConsultationEvent(final String transportSessionId, ConsultationEventDTO<SessionAbandonedDTO> consultationEventDTO);
}
