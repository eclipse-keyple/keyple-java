/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.remote_se.transport;

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

    public KeypleDTO(String action, String body, Boolean isRequest, String sessionId) {
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
