package com.woow.axsalud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/websocket")
@Tag(name = "WebSocket Documentation", description = "WebSocket messaging flow for Consultation sessions using STOMP")
public class WebSocketDocsController {

    @GetMapping("/ws-docs/consultation")
    @Operation(
            summary = "STOMP WebSocket Messaging Flow for Consultation",
            description = """
            ### 🔄 WebSocket Consultation Messaging Flow
            
            This describes the full message sequence between a **React Frontend Client** and the **Spring Boot backend** using STOMP over WebSocket.
            
            ---
            
            ### 📡 WebSocket Endpoint
            
            - `wss://{your-domain}/ws`
            
            > Use STOMP over WebSocket, secure via HTTPS.

            ### 🔐 Authentication
            
            - All STOMP frames **must include** `Authorization: Bearer <JWT>` header.
            - Required on initial `CONNECT` frame and preserved during session.

            ---
            
            ### 1️⃣ Patient Flow
            
            1. **Register** via `POST /api/woo_user/new`
            2. **Login** and get JWT token
            3. **Start Consultation** via `POST /api/consultation`
            4. **Connect WebSocket**:
                - Destination: `/user/queue/messages` (subscribe)
                - Send: `/app/consultation/{consultationId}/private`
            5. **Send Message** using payload:
            
            ```json
            {
              "consultationId": "abc-123",
              "content": "Hello from patient",
              "receiver": "doctor@email.com"
              "sender": "patientUser@ex.com"
            }
            ```

            ---
            
            ### 2️⃣ Doctor Flow
            
            1. **Register** via `POST /api/health_provider/new`
            2. **Login** and get JWT
            3. **Accept Consultation** via:
               ```
               PUT /api/consultation/{consultationId}/doctor
               ```
            4. **Connect WebSocket** and subscribe to:
               ```
               /user/queue/messages
               ```
            5. **Receive messages** from the patient
            6. **Send replies** via:
               ```
               /app/consultation/{consultationId}/private
               ```

            ---
            
            ### 💬 Messaging Protocol

            - All messages are JSON payloads matching `ConsultationMessage` schema.
            - Each message is routed through:
                ```
                @MessageMapping("/consultation/{consultationId}/private")
                ```

            ---
            
            ### 📥 Subscriptions

            - Patients and Doctors both subscribe to:
                ```
                /user/queue/messages
                ```
              where private replies will be delivered based on their session + principal.

            ---
            
            ### 🔁 Example STOMP Send (raw)
            
            ```
            SEND
            destination:/app/consultation/123/private
            Authorization: Bearer eyJhbGciOiJIUzI1...
            
            {
              "consultationId": "123",
              "content": "Hello Patient",
              "receiver": "patient@email.com"
              "sender": "doctor@ex.com"
            }
            ```

            ---
            
            ### 📎 Notes
            - `consultationId` is required in both send and receive.
            - WebSocket session is validated with the token on each frame.
            - STOMP subscriptions are per-user and isolated.

            ---
            ⚠️ Ensure your frontend adds the JWT token in the STOMP CONNECT headers, like:
            ```js
            stompClient.connect({ Authorization: 'Bearer <token>' }, ...)
            ```
            """
    )
    public ResponseEntity<?> websocketDocs() {
        return ResponseEntity.ok().build();
    }
}
