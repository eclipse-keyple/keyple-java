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

/**
 * Create Virtual Reader Sessions
 */
class VirtualReaderSessionFactory {



    /**
     * Create a new session (internal method used by VirtualReader)
     * 
     * @param nativeReaderName
     * @param slaveNodeId slave device slaveNodeId
     * @return Session for this reader
     */
    public VirtualReaderSession createSession(String nativeReaderName, String slaveNodeId,
            String masterNodeId) {
        return new VirtualReaderSessionImpl(generateSessionId(nativeReaderName, slaveNodeId),
                slaveNodeId, masterNodeId);
    }


    /*
     * PRIVATE METHODS
     */

    /**
     * Generate a unique sessionId for a new connecting localreader
     * 
     * @param nativeReaderName : Local Reader Name
     * @param nodeId : Node Id from which the local reader name connect to
     * @return unique sessionId
     */
    private String generateSessionId(String nativeReaderName, String nodeId) {
        return nativeReaderName + nodeId + String.valueOf(System.currentTimeMillis());
    }


}
