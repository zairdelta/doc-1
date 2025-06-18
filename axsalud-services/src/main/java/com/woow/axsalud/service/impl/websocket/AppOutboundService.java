package com.woow.axsalud.service.impl.websocket;

import com.woow.axsalud.service.api.dto.ConsultationDTO;
import com.woow.axsalud.service.api.messages.ConsultationEventDTO;
import com.woow.axsalud.service.api.messages.control.ControlMessageDTO;
import com.woow.axsalud.service.api.messages.control.SessionAbandonedDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class AppOutboundService {

   // @Value {}
    private long X_MESSAGE_TTL = 120000;
    private long X_EXPIRES = 3600000;


    private final static String QUEUE_MESSAGES_DESTINATION = "/queue/messages";
    private final static String QUEUE_ERRORS = "/queue/errors";
    private final static String DOCTOR_EVENTS_DESTINATION = "/topic/doctor-events";
    private SimpMessagingTemplate messagingTemplate;
    public AppOutboundService(final SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendQueueMessage(String receiverEmail, ConsultationEventDTO consultationEventDTO,
                                 String consultationSessionId) {

        String queueName = buildQueueNameFromEmail(receiverEmail, consultationSessionId); // e.g. "dandoctor-example-com_user_queue_messages-queue"
        String destination = "/queue/" + queueName;

        Map<String, Object> headers = new HashMap<>();
        headers.put("x-message-ttl", X_MESSAGE_TTL);
        headers.put("x-expires", X_EXPIRES);
        log.info("{}_ Sending message to {}, receiver: {}, messageId: {}",
                consultationEventDTO.getTransportSessionId(),
                destination,
                receiverEmail,
                consultationEventDTO.getId());

        messagingTemplate.convertAndSend(destination, consultationEventDTO, headers);
    }

    private String buildQueueNameFromEmail(String email, String consultationSessionId) {

        return email
                .replace("@", "_")
                .replace(".", "_")
                + "_" + consultationSessionId + "_user_queue_messages-queue";
    }

    public void sendErrorQueueMessage(String receiver, ConsultationEventDTO consultationEventDTO) {
        log.info("{}_ Sending message to {}, receiver: {}, messageId: {}",
                consultationEventDTO.getTransportSessionId(),
                QUEUE_ERRORS,
                receiver,
                consultationEventDTO.getId());
        messagingTemplate.convertAndSendToUser(
                receiver,
                QUEUE_ERRORS,
                consultationEventDTO
        );
    }
    public void sendDoctorEventMessage(ConsultationEventDTO consultationEventDTO) {
        log.info("{}_ Sending consultationEventDTO to topic/doctor-events: {}",
                consultationEventDTO.getTransportSessionId(), consultationEventDTO);
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

    public String sendSessionAbandonedConsultationControlEvent(String transportSessionId, String consultationId,
                                             String consultationSessionId,
                                             ConsultationEventDTO<SessionAbandonedDTO> consultationEventDTO) {
        String controlComunicationTopic = "/topic/consultation." + consultationId +
                ".session." + consultationSessionId + ".control";
        log.debug("{}_ Sending sessionAbandonedDTO to topic: {} ", transportSessionId, controlComunicationTopic);
        messagingTemplate.convertAndSend(controlComunicationTopic, consultationEventDTO);
        log.debug("sessionAbandonedDTO sent to topic: {}, message: {} ", controlComunicationTopic, consultationEventDTO);
        return controlComunicationTopic;
    }

    private String generateMessageId(ConsultationEventDTO event) {
        return event.getId() + "-" + System.currentTimeMillis();
    }

}
