package com.woow.axsalud.service.api;

import org.springframework.web.multipart.MultipartFile;

public interface LocalUserCsvImportService {
    void importFromCsv(MultipartFile file, String serviceProviderName) throws Exception;
}
