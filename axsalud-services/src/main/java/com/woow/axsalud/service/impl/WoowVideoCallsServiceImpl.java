package com.woow.axsalud.service.impl;

import com.woow.axsalud.data.consultation.ConsultationSession;
import com.woow.axsalud.service.api.ConsultationService;
import com.woow.axsalud.service.api.WoowVideoCallsService;
import com.woow.axsalud.service.api.dto.ConsultationMessgeTypeEnum;
import com.woow.axsalud.service.api.dto.VideoTokenDTO;
import com.woow.axsalud.service.api.exception.ConsultationServiceException;
import com.woow.axsalud.service.api.exception.WoowVideoCallException;
import com.woow.axsalud.service.api.messages.ConsultationEventDTO;
import com.woow.axsalud.service.api.messages.VideoCallStartMessageDTO;
import io.agora.media.RtcTokenBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class WoowVideoCallsServiceImpl implements WoowVideoCallsService {

    @Value("${AGORA_APP_ID}")
    private  String APP_ID;

    @Value("${AGORA_APP_CERTIFICATE}")
    private  String APP_CERTIFICATE;

    @Value("${TOKEN_VIDEO_CALL_DURATION_IN_SECONDS:600}")
    private int CALL_DURATION;

    private SimpMessagingTemplate messagingTemplate;

    private ConsultationService consultationService;

    public WoowVideoCallsServiceImpl(SimpMessagingTemplate messagingTemplate,
                                     ConsultationService consultationService
                                     ) {
        this.messagingTemplate = messagingTemplate;
        this.consultationService = consultationService;
    }

    @Override
    public VideoTokenDTO create(String consultationSessionId) throws WoowVideoCallException {

        ConsultationSession consultationSession =
                null;
        try {
            consultationSession = consultationService.getConsultationSession(consultationSessionId);
        } catch (ConsultationServiceException e) {
            throw new WoowVideoCallException("invalid consultation SessionId," +
                    " getting session: " + consultationSessionId, 403);

        }

        if(consultationSession == null) {
            throw new WoowVideoCallException("invalid consultation SessionId: "
                    + consultationSessionId, 403);
        }

        RtcTokenBuilder tokenBuilder = new RtcTokenBuilder();
        String channelName = consultationSessionId + UUID.randomUUID().toString().substring(0, 4);
        int timestamp = (int)(System.currentTimeMillis() / 1000) + CALL_DURATION;
        String token = tokenBuilder.buildTokenWithUid(APP_ID, APP_CERTIFICATE,
                channelName, 0, RtcTokenBuilder.Role.Role_Publisher, timestamp);
        VideoTokenDTO videoTokenDTO = new VideoTokenDTO();
        videoTokenDTO.setAccessToken(token);
        videoTokenDTO.setChannelName(channelName);

        String patientUserName =
                consultationSession.getConsultation().getPatient().getCoreUser().getUserName();
        String doctorUserName = consultationSession.getDoctor().getCoreUser().getUserName();

        VideoCallStartMessageDTO consultationMessageDTO = new VideoCallStartMessageDTO();
        consultationMessageDTO.setContent(token);
        consultationMessageDTO.setVideoTokenDTO(videoTokenDTO);
        consultationMessageDTO.setSender(patientUserName);
        consultationMessageDTO.setReceiver(doctorUserName);
        consultationMessageDTO
                .setConsultationId(consultationSession.getConsultation().getConsultationId().toString());
        consultationMessageDTO.setConsultationSessionId(consultationSessionId);

        ConsultationEventDTO<VideoCallStartMessageDTO> consultationEventDTO = new ConsultationEventDTO<>();
        consultationEventDTO.setMessageType(ConsultationMessgeTypeEnum.START_VIDEO_CALL);
        consultationEventDTO.setTimeProcessed(LocalDateTime.now());
        consultationEventDTO.setPayload(consultationMessageDTO);
        consultationEventDTO.setId(0);
        messagingTemplate.convertAndSendToUser(
                doctorUserName,
                "/queue/messages",
                consultationEventDTO);

        messagingTemplate.convertAndSendToUser(
                patientUserName,
                "/queue/messages",
                consultationEventDTO);

        return videoTokenDTO;
    }
}
