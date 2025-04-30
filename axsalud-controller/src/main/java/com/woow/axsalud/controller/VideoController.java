package com.woow.axsalud.controller;

import com.woow.axsalud.controller.exception.WooBoHttpError;
import com.woow.axsalud.service.api.WoowVideoCallsService;
import com.woow.axsalud.service.api.dto.VideoTokenDTO;
import com.woow.axsalud.service.api.exception.WoowVideoCallException;
import io.agora.media.RtcTokenBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/video")
@Validated
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Patient User Endpoint", description = "Operations related to patient users")
public class VideoController {

    private WoowVideoCallsService woowVideoCallsService;
    public VideoController(WoowVideoCallsService woowVideoCallsService) {
        this.woowVideoCallsService = woowVideoCallsService;
    }
    @GetMapping("/{consultationSessionId}/accessToken")
    public ResponseEntity<VideoTokenDTO> getAccessToken(@PathVariable String consultationSessionId) {

        try {
            return ResponseEntity.ok().body(woowVideoCallsService.create(consultationSessionId));
        } catch (WoowVideoCallException e) {
            return WooBoHttpError.of(e).toResponseEntity();
        }
    }

}
