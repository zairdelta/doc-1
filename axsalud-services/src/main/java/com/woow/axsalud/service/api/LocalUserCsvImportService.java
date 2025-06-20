package com.woow.axsalud.service.api;

import com.woow.axsalud.service.api.dto.CsvUserDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LocalUserCsvImportService {
    void importFromCsv(List<CsvUserDTO> users, String serviceProviderName) throws Exception;
}
