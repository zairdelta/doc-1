package com.woow.axsalud.service.api;

import com.woow.axsalud.data.client.AxSaludWooUser;
import com.woow.axsalud.data.client.PatientData;
import com.woow.axsalud.data.consultation.ConsultationDocument;
import com.woow.axsalud.data.consultation.ConsultationSession;
import com.woow.axsalud.data.repository.PatientConsultationSummary;
import com.woow.axsalud.service.api.dto.*;
import com.woow.axsalud.service.api.exception.ConsultationServiceException;
import com.woow.axsalud.service.api.messages.ConsultationEventDTO;
import com.woow.axsalud.service.api.messages.ConsultationMessageDTO;
import com.woow.core.data.user.WoowUser;
import com.woow.core.service.api.exception.WooUserServiceException;
import com.woow.storage.api.StorageServiceException;
import com.woow.storage.api.StorageServiceUploadResponseDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AxSaludService {
    String save(AxSaludUserDTO axSaludUserDTO) throws WooUserServiceException;
    PatientViewDTO get(String userName) throws WooUserServiceException;

    List<DoctorPrescriptionViewDTO>  getDoctorPrescriptions(String userName) throws WooUserServiceException;
    List<LabPrescriptionViewDTO>  getLabPrescriptions(String userName) throws WooUserServiceException;
    List<ConsultationDTO> getConsultation(String userName) throws WooUserServiceException;

    void updatePatientData(String userName,  PatientDataDTO patientDataDTO) throws WooUserServiceException;
    String update(String userName, AxSaludUserUpdateDTO patientDataUpdateDTO) throws WooUserServiceException;
    List<PatientConsultationSummary> getUserHistory(String userName, int pageNumber,
                                                    int totalElementsPerPage);
    FileResponseDTO appendDocument(String userName, MultipartFile file) throws ConsultationServiceException, WooUserServiceException;
}
