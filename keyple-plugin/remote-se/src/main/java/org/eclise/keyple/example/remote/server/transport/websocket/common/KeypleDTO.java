package org.eclise.keyple.example.remote.server.transport.websocket.common;

public class KeypleDTO {

    String sessionId;
    String action;
    String body;
    Integer hash;
    Boolean isRequest;

    public KeypleDTO(String action, String body, Boolean isRequest) {
        this.action = action;
        this.body = body;
        this.isRequest = isRequest;
    }

    public KeypleDTO(String sessionId, String action, String body, Boolean isRequest) {
        this.sessionId = sessionId;
        this.action = action;
        this.body = body;
        this.isRequest = isRequest;
    }

    public Boolean isRequest() {
        return isRequest;
    }

    public String getAction() {
        return action;
    }

    public String getBody() {
        return body;
    }

    public Integer getHash() {
        return hash;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setHash(Integer hash) {
        this.hash = hash;
    }
}
