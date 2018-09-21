/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.remote_se.transport;

/**
 * Data Transfer Tbject used to tranport a API call from a Master reader to a Slave Reader (or reverse)
 * POJO
 */
public class KeypleDTO {

    //Master reader session
    String sessionId;
    //API method to be called
    String action;
    //Arguments of the API (json)
    String body;
    //Is a request or a response
    Boolean isRequest;
    //Integrity hash (not in used yet)
    Integer hash;

    /**
     * Basic Constructor
     * @param action
     * @param body
     * @param isRequest
     */
    public KeypleDTO(String action, String body, Boolean isRequest) {
        this.action = action;
        this.body = body;
        this.isRequest = isRequest;
    }
    /**
     * Constructor with a Virtual Reader Session Id
     * @param action
     * @param body
     * @param isRequest
     * @param sessionId : Session Id of current Virtual Reader Session Id
     */
    public KeypleDTO(String action, String body, Boolean isRequest, String sessionId) {
        this.sessionId = sessionId;
        this.action = action;
        this.body = body;
        this.isRequest = isRequest;
    }

    /*
    Getters and Setters
     */

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
