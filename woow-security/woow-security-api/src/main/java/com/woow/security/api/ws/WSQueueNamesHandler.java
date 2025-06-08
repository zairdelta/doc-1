package com.woow.security.api.ws;

public interface WSQueueNamesHandler {
    String parseQueueNameFrom(String sessionId, String subscriptioinId);
}
