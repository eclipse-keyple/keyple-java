/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.server.transport.async.webservice.server;

import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclise.keyple.example.remote.server.transport.async.AsyncRSEReaderSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage RSE Reader Session Manage SeRequestSet to transmit and receive SeResponseSet in an
 * asynchronous way
 */
public class WsRSEReaderSession implements AsyncRSEReaderSession {

    private static final Logger logger = LoggerFactory.getLogger(WsRSEReaderSession.class);

    String sessionId;
    SeRequestSet seRequestSet;
    SeResponseSetCallback seResponseSetCallback;


    // constructor
    public WsRSEReaderSession(String sessionId) {
        this.sessionId = sessionId;
    }



    /**
     * Manage the asynchronous transmit
     * 
     * @param seRequestSet : seRequestSet to be processed
     * @param seResponseSetCallback : callback that will be called once seResponseSet is received
     */
    public void asyncTransmit(SeRequestSet seRequestSet,
            SeResponseSetCallback seResponseSetCallback) {
        logger.debug("Session {} asyncTransmit {}", sessionId, seRequestSet);
        if (this.seRequestSet == null) {
            logger.debug("Set a new seRequestSet in Session {}", sessionId);
            this.seRequestSet = seRequestSet;
            this.seResponseSetCallback = seResponseSetCallback;
        } else {
            logger.warn("SeRequestSet is already set in Session {}", sessionId);

        }
    }

    /**
     * Receive the seResponseSet
     * 
     * @param seResponseSet
     */
    @Override
    public void asyncSetSeResponseSet(SeResponseSet seResponseSet) {
        logger.debug("Session {} asyncSetSeResponseSet {}", sessionId, seResponseSet);
        if (this.seRequestSet != null) {
            // todo check that responseSet is matching requestSet

            // release seRequestSet next work
            this.seRequestSet = null;
            // return seResponseSet by callback
            this.seResponseSetCallback.getResponseSet(seResponseSet);
        } else {
            logger.warn(
                    "Session has been lost, seRequestSet is missing while receiving seResponseSet {}",
                    seResponseSet);

        }
    }

    @Override
    public Boolean hasSeRequestSet() {
        return seRequestSet != null;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public SeRequestSet getSeRequestSet() {
        return this.seRequestSet;
    }
}
