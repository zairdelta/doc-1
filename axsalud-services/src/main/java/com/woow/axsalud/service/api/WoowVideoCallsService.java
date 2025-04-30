package com.woow.axsalud.service.api;

import com.woow.axsalud.service.api.dto.VideoTokenDTO;
import com.woow.axsalud.service.api.exception.WoowVideoCallException;
import org.springframework.web.bind.annotation.PathVariable;

public interface WoowVideoCallsService {
    VideoTokenDTO create(@PathVariable String consultationSessionId) throws WoowVideoCallException;
}
