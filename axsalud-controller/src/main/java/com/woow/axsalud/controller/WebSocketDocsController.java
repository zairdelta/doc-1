package com.woow.axsalud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/websocket")
@Tag(name = "WebSocket Documentation")
public class WebSocketDocsController {

    @GetMapping("/ws-docs/consultation")
    @Operation(
            summary = "WebSocket Consultation Endpoint",
            description = """
            This is a WebSocket endpoint using STOMP protocol.
            
            - Destination: `/app/consultation/{consultationId}/private`
            - Send a `ConsultationMessage` to the specified consultation room.
            - Messages are broadcast to subscribed clients.
            - Use a valid Bearer token in the STOMP `Authorization` header.
            """
    )
    public ResponseEntity websocketDocs() {
        return ResponseEntity.ok().build();
    }
}
