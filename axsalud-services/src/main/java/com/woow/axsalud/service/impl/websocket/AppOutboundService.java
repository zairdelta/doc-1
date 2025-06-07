package com.woow.axsalud.service.impl.websocket;

import com.woow.axsalud.service.api.dto.ConsultationDTO;
import com.woow.axsalud.service.api.messages.ConsultationEventDTO;
import com.woow.axsalud.service.api.messages.control.ControlMessageDTO;
import com.woow.axsalud.service.api.messages.control.SessionAbandonedDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AppOutboundService {

    private final static String QUEUE_MESSAGES_DESTINATION = "/queue/messages";
    private final static String QUEUE_ERRORS = "/queue/errors";
    private final static String DOCTOR_EVENTS_DESTINATION = "/topic/doctor-events";
    private SimpMessagingTemplate messagingTemplate;
    public AppOutboundService(final SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendQueueMessage(String receiver, ConsultationEventDTO consultationEventDTO) {
        log.info("Sending message to QUEUE_MESSAGES_DESTINATION, receiver: {}, messageId: {}", receiver,
                consultationEventDTO.getId());
        messagingTemplate.convertAndSendToUser(
                receiver,
                QUEUE_MESSAGES_DESTINATION,
                consultationEventDTO,
                message -> {
                    // Add a custom message ID or any other metadata
                    message.getHeaders().put("x-msg-id", generateMessageId(consultationEventDTO));
                    return message;
                }
        );
    }

    public void sendErrorQueueMessage(String receiver, ConsultationEventDTO consultationEventDTO) {
        messagingTemplate.convertAndSendToUser(
                receiver,
                QUEUE_ERRORS,
                consultationEventDTO
        );
    }
    public void sendDoctorEventMessage(ConsultationEventDTO consultationEventDTO) {
        log.info("Sending consultationEventDTO to topic/doctor-events: {}", consultationEventDTO);
        messagingTemplate.convertAndSend(DOCTOR_EVENTS_DESTINATION, consultationEventDTO);
    }

    public void sendDoctorEventMessage(ConsultationDTO consultationDTO) {
        log.info("Sending consultationDTO to topic/doctor-events: {}", consultationDTO);
        messagingTemplate.convertAndSend(DOCTOR_EVENTS_DESTINATION, consultationDTO);
    }

    public void sendConsultationControlEvent(String consultationId,
                                             String consultationSessionId,
                                             ControlMessageDTO controlMessageDTO) {
        String controlComunicationTopic = "/topic/consultation." + consultationId +
                ".session." + consultationSessionId + ".control";
        log.debug("Sending controleMessage to topic: {} ", controlComunicationTopic);
        messagingTemplate.convertAndSend(controlComunicationTopic, controlMessageDTO);
        log.debug("ControlMessage sent to topic: {}, message: {} ", controlComunicationTopic, controlMessageDTO);

    }

    public String sendSessionAbandonedConsultationControlEvent(String consultationId,
                                             String consultationSessionId,
                                             ConsultationEventDTO<SessionAbandonedDTO> consultationEventDTO) {
        String controlComunicationTopic = "/topic/consultation." + consultationId +
                ".session." + consultationSessionId + ".control";
        log.debug("Sending sessionAbandonedDTO to topic: {} ", controlComunicationTopic);
        messagingTemplate.convertAndSend(controlComunicationTopic, consultationEventDTO);
        log.debug("sessionAbandonedDTO sent to topic: {}, message: {} ", controlComunicationTopic, consultationEventDTO);
        return controlComunicationTopic;
    }

    private String generateMessageId(ConsultationEventDTO event) {
        return event.getId() + "-" + System.currentTimeMillis();
    }

}
