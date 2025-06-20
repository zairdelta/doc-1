package com.woow.axsalud.controller;

import com.opencsv.bean.CsvToBeanBuilder;
import com.woow.axsalud.service.api.LocalUserCsvImportService;
import com.woow.axsalud.service.api.dto.CsvUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.util.List;

@RestController
@RequestMapping("/api/controlpanel")
@Slf4j
public class ControlPanelController {
    private final LocalUserCsvImportService importService;

    public ControlPanelController(LocalUserCsvImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/csv")
    public ResponseEntity uploadCsv(@RequestParam("file") MultipartFile file,
                                    @RequestParam("serviceProviderName") String serviceProviderName) {
        try {
            List<CsvUserDTO> users = new CsvToBeanBuilder<CsvUserDTO>(
                    new InputStreamReader(file.getInputStream()))
                    .withType(CsvUserDTO.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withThrowExceptions(true)
                    .build()
                    .parse();
            importService.importFromCsv(users, serviceProviderName);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
