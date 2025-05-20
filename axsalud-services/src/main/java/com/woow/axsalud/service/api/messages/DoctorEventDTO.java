package com.woow.axsalud.service.api.messages;

import com.woow.axsalud.service.api.dto.ConsultationDTO;
import lombok.Data;

@Data
public class DoctorEventDTO extends ConsultationEventDTO<DoctorEventDTO> {
    private ConsultationDTO consultationDTO;
}
