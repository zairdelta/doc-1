package com.woow.axsalud.service.impl;

import com.opencsv.bean.CsvToBeanBuilder;
import com.woow.axsalud.data.repository.LocalServiceProviderUserRepository;
import com.woow.axsalud.data.repository.ServiceProviderRepository;
import com.woow.axsalud.data.serviceprovider.LocalServiceProviderUserEntity;
import com.woow.axsalud.data.serviceprovider.ServiceProvider;
import com.woow.axsalud.service.api.LocalUserCsvImportService;
import com.woow.axsalud.service.api.dto.CsvUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class LocalUserCsvImportServiceImpl implements LocalUserCsvImportService {
    private final LocalServiceProviderUserRepository repository;
    private final ServiceProviderRepository serviceProviderRepository;

    public LocalUserCsvImportServiceImpl(LocalServiceProviderUserRepository repository,
                                         ServiceProviderRepository serviceProviderRepository) {
        this.repository = repository;
        this.serviceProviderRepository = serviceProviderRepository;
    }

    @Transactional
    @Async
    public void importFromCsv(MultipartFile file, String serviceProviderName) throws Exception {

        ServiceProvider serviceProvider =
                serviceProviderRepository.findByName(serviceProviderName);

        if(serviceProvider == null) {
            throw new Exception("Service provider does not exist");
        }

        long serviceProviderId = serviceProvider.getId();

        List<CsvUserDTO> users = new CsvToBeanBuilder<CsvUserDTO>(
                new InputStreamReader(file.getInputStream()))
                .withType(CsvUserDTO.class)
                .withIgnoreLeadingWhiteSpace(true)
                .withThrowExceptions(true)
                .build()
                .parse();

        for (CsvUserDTO dto : users) {

            LocalServiceProviderUserEntity localServiceProviderUserEntity =
                    repository.findByServiceProviderIdAndHid(serviceProviderId, dto.getHid());

            if(localServiceProviderUserEntity != null) {
                LocalServiceProviderUserEntity
                        entity = new LocalServiceProviderUserEntity();
                entity.setHid(dto.getHid());
                entity.setName(dto.getName());
                entity.setLastName(dto.getLastName());
                entity.setCreatedAt(LocalDateTime.now());
                entity.setUserValid(0);
                entity.setUserId(0);
                entity.setServiceProviderId(serviceProvider.getId());

                repository.save(entity);
            } else {
                log.warn("skiping user: {}, is already in DB", dto);
            }
        }
    }
}
