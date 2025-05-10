🔄 How STOMP messages are processed in Spring WebSocket
1. 📥 Client sends STOMP message:
   Example raw frame sent by the browser/client:

pgsql
Copy
Edit
SEND
destination:/app/chat
content-type:application/json
Authorization:Bearer <JWT>

{"content": "Hi doctor", "from": "patient"}
2. 🧠 Spring intercepts the frame via WebSocketHandler & converts it
   The message goes through:

WebSocketStompClient

SubProtocolWebSocketHandler

StompSubProtocolHandler

MessageChannel chain

Your @MessageMapping method (if destination matches /app/...)

3. 💬 Your Controller receives something like:
   java
   Copy
   Edit
   @MessageMapping("/chat")
   public void handleChatMessage(ChatMessage message,
   @Header("simpSessionId") String sessionId,
   Principal user) {
   // message: {"content": "Hi", "from": "user1"}
   // headers are mapped automatically
   }
   🚫 So: Does the application ever get the raw STOMP frame?
   No.
   By design, Spring handles and parses the protocol so that the application can focus on business logic — not frame parsing.

If you need the original STOMP content (for logging/debugging/etc.), you’d have to intercept it at a lower level, like:

ChannelInterceptor

StompSubProtocolHandler override (advanced)

📦 What content can you still access?
You can access:

simpDestination

simpSessionId

simpUser

Any custom headers (Authorization, etc.)

The parsed payload

But you don’t get the STOMP command + raw body unless you explicitly intercept earlier.

Spring Messaging (Spring WebSocket module) is the one implementing the STOMP protocol handling.

The class StompSubProtocolHandler (in Spring Messaging) is what parses the raw STOMP frame into a Spring Message<?> and routes it accordingly.

The converted message is then handled by the SimpAnnotationMethodMessageHandler if it matches your setApplicationDestinationPrefixes() (like /app).

If the destination doesn't match the app prefixes (e.g., it’s /topic, /queue), Spring doesn't route it to a controller — it goes straight to the SimpleBrokerMessageHandler or StompBrokerRelayMessageHandler, depending on your config.

| Layer                  | Purpose                                | How to Customize                                  |
| ---------------------- | -------------------------------------- | ------------------------------------------------- |
| HTTP Handshake         | Initial handshake (auth)               | `HandshakeInterceptor`, custom `HandshakeHandler` |
| WebSocket Raw Messages | Raw WebSocket events                   | `WebSocketHandlerDecoratorFactory`                |
| STOMP Parsing          | Convert STOMP → Spring Message         | (internal) use `ChannelInterceptor` instead       |
| Message Routing        | Before hitting `@MessageMapping`, etc. | `ChannelInterceptor` on `clientInboundChannel`    |
| Application Logic      | Controller-level logic                 | `@MessageMapping`, `@SendToUser`, etc.            |
| Broker Communication   | Message relay to RabbitMQ, etc.        | Configuration only (`.enableStompBrokerRelay`)    |
