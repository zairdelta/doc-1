package com.woow.axsalud.service.api.websocket;

import com.woow.axsalud.service.api.messages.control.ControlMessage;
import com.woow.axsalud.service.api.messages.control.ControlMessageType;
import com.woow.security.api.WebSocketUserPrincipal;


public interface ControlMessageHandler {

    boolean supports(ControlMessageType type);
    void handledControlMessage(final ControlMessage message);
}
