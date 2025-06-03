package com.woow.axsalud.service.impl.websocket.control;

import com.woow.axsalud.service.api.messages.control.ControlMessage;
import com.woow.axsalud.service.api.websocket.ControlMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ControlMessageDispatcher {
    private List<ControlMessageHandler> controMessageHandlers;

    public ControlMessageDispatcher(List<ControlMessageHandler> controMessageHandlers) {
        this.controMessageHandlers = controMessageHandlers;
        log.debug("Dispatcher handlers found: {}", controMessageHandlers);
    }

    public void dispatch(ControlMessage message) {
        log.debug("Finding correct hanblder for message: {}", message);
        controMessageHandlers.stream()
                .filter(controlMessageHandler -> controlMessageHandler.supports(message.getControlMessageDTO().getMessageType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported message type: " + message.getControlMessageDTO().getMessageType()))
                .handledControlMessage(message);
    }

}
