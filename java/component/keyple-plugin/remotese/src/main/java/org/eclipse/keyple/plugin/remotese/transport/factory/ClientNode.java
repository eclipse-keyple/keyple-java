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


/**
 * Client type of TransportNode, connects to a ServerNode
 */
public interface ClientNode extends TransportNode {

    void connect(ConnectCallback connectCallback);

    void disconnect();

    interface ConnectCallback {
        void onConnectSuccess();

        void onConnectFailure();
    }

}
