/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.remote_se.rse;

import org.eclipse.keyple.plugin.remote_se.transport.DtoSender;
import org.eclipse.keyple.plugin.remote_se.transport.KeypleDTO;
import org.eclipse.keyple.plugin.remote_se.transport.KeypleDTOHelper;
import org.eclipse.keyple.plugin.remote_se.transport.json.SeProxyJsonParser;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage RSE Reader Session Manage SeRequestSet to transmit and receive SeResponseSet in an
 * asynchronous way
 */
public class ReaderAsyncClientImpl implements IReaderAsyncSession {

    private static final Logger logger = LoggerFactory.getLogger(ReaderAsyncClientImpl.class);

    String sessionId;
    SeRequestSet seRequestSet;
    ISeResponseSetCallback seResponseSetCallback;
    DtoSender dtoSender;

    // constructor
    public ReaderAsyncClientImpl(String sessionId, DtoSender dtoSender) {
        this.sessionId = sessionId;
        this.dtoSender = dtoSender;
    }

    /**
     * Manage the asynchronous transmit
     * 
     * @param seRequestSet : seRequestSet to be processed
     * @param seResponseSetCallback : callback that will be called once seResponseSet is received
     */
    public void asyncTransmit(SeRequestSet seRequestSet,
            ISeResponseSetCallback seResponseSetCallback) {
        logger.debug("Session {} asyncTransmit {}", sessionId, seRequestSet);
        if (this.seRequestSet == null) {
            logger.debug("Set a new seRequestSet in Session {}", sessionId);
            this.seRequestSet = seRequestSet;
            this.seResponseSetCallback = seResponseSetCallback;

            dtoSender.sendDTO(new KeypleDTO(
                    KeypleDTOHelper.READER_TRANSMIT,
                    SeProxyJsonParser.getGson().toJson(this.seRequestSet, SeRequestSet.class),
                    true,
                    sessionId));

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
    public String getTransmitUrl() {
        return null;
    }

    @Override
    public SeRequestSet getSeRequestSet() {
        return this.seRequestSet;
    }


    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public Boolean isAsync() {
        return true;
    }

}
