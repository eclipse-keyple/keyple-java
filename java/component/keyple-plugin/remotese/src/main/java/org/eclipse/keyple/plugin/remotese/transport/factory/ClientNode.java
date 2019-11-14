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
package org.eclipse.keyple.plugin.remotese.transport.factory;


import org.eclipse.keyple.plugin.remotese.transport.DtoNode;

/**
 * Client type of DtoNode, connects to a ServerNode
 */
public interface ClientNode extends DtoNode {

    /**
     * Connect to the server
     * 
     * @param connectCallback
     */
    void connect(ConnectCallback connectCallback);

    /**
     * Disconnect from the server
     */
    void disconnect();

    /**
     * Retrieve ServerNodeId
     * 
     * @return
     */
    String getServerNodeId();

    /**
     * Callback on the connection success
     */
    interface ConnectCallback {
        /**
         * Called if the connection is sucessful
         */
        void onConnectSuccess();

        /**
         * Called if the connection has failed
         */
        void onConnectFailure();
    }

}
