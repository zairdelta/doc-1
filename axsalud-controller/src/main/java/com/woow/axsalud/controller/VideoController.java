package com.woow.axsalud.controller;

import com.woow.axsalud.service.api.dto.VideoTokenDTO;
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

    @Value("${AGORA_APP_ID}")
    private  String APP_ID;

    @Value("${AGORA_APP_CERTIFICATE}")
    private  String APP_CERTIFICATE;

    @Value("{TOKEN_VIDEO_CALL_DURATION_IN_SECONDS:600}")
    private int CALL_DURATION;
    @GetMapping("/{consultationSessionId}/accessToken")
    public ResponseEntity<VideoTokenDTO> getAccessToken(@PathVariable String consultationSessionId) {

        RtcTokenBuilder tokenBuilder = new RtcTokenBuilder();
        int timestamp = (int)(System.currentTimeMillis() / 1000) + CALL_DURATION;
        String token = tokenBuilder.buildTokenWithUid(APP_ID, APP_CERTIFICATE,
                consultationSessionId, 0, RtcTokenBuilder.Role.Role_Publisher, timestamp);
        VideoTokenDTO videoTokenDTO = new VideoTokenDTO();
        videoTokenDTO.setAccessToken(token);
        videoTokenDTO.setChannelName(consultationSessionId);
        return ResponseEntity.ok().body(videoTokenDTO);
    }

}
