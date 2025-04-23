package com.woow.axsalud.service.api;

import com.woow.axsalud.data.client.PatientData;
import com.woow.axsalud.service.api.dto.AxSaludUserDTO;
import com.woow.axsalud.service.api.dto.PatientViewDTO;
import com.woow.core.service.api.exception.WooUserServiceException;

public interface AxSaludService {
    String save(AxSaludUserDTO axSaludUserDTO) throws WooUserServiceException;
    PatientViewDTO get(String userName) throws WooUserServiceException;

    void updatePatientData(String userName, PatientData patientData) throws WooUserServiceException;
}
