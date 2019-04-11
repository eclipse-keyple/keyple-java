/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.remotese.pluginse;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage RSE Reader Session
 */
public class VirtualReaderSessionImpl implements VirtualReaderSession {

    private static final Logger logger = LoggerFactory.getLogger(VirtualReaderSessionImpl.class);

    private final String sessionId;
    private final String slaveNodeId;
    private final String masterNodeId;

    // constructor
    public VirtualReaderSessionImpl(String sessionId, String slaveNodeId, String masterNodeId) {
        logger.debug("Creating VirtualReader sessionId:{} slaveNodeId:{} slaveNodeId:{}", sessionId,
                slaveNodeId, slaveNodeId);
        if (sessionId == null) {
            throw new IllegalArgumentException("SessionId must not be null");
        }
        if (masterNodeId == null) {
            throw new IllegalArgumentException("MasterNodeId must not be null");
        }
        if (slaveNodeId == null) {
            throw new IllegalArgumentException("SlaveNodeId must not be null");
        }
        this.sessionId = sessionId;
        this.slaveNodeId = slaveNodeId;
        this.masterNodeId = masterNodeId;
    }


    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String getSlaveNodeId() {
        return slaveNodeId;
    }


    @Override
    public String getMasterNodeId() {
        return masterNodeId;
    }

    @Override
    public String toString() {
        return "sessionId:" + sessionId + " - slaveNodeId:" + slaveNodeId + " - masterNodeId:"
                + masterNodeId;
    }
}
