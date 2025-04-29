package com.woow.axsalud.service.api.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ConsultationMessagesPagingDTO {
    private List<ConsultationMessageDTO> messages = new ArrayList();
    private long totalElements;
    private long totalPages;

}
