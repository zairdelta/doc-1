package com.woow.axsalud.service.api;

import com.woow.axsalud.data.client.PatientData;
import com.woow.axsalud.data.repository.PatientConsultationSummary;
import com.woow.axsalud.service.api.dto.*;
import com.woow.axsalud.service.api.exception.ConsultationServiceException;
import com.woow.core.service.api.exception.WooUserServiceException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

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
}
