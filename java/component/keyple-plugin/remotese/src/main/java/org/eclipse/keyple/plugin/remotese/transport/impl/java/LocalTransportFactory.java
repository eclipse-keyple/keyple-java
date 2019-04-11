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
package org.eclipse.keyple.plugin.remotese.transport.impl.java;



import org.eclipse.keyple.plugin.remotese.transport.factory.ClientNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.ServerNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportFactory;

public class LocalTransportFactory extends TransportFactory {

    /*
     * static private LocalTransportFactory instance = new LocalTransportFactory(); private
     * LocalTransportFactory(){} static public LocalTransportFactory instance(){ return instance; }
     */

    private final LocalServer theServer;

    public LocalTransportFactory(String serverNodeId) {
        theServer = new LocalServer(serverNodeId);
    }

    @Override
    public ClientNode getClient(String clientNodeId) {
        return new LocalClient(clientNodeId, theServer);
    }

    @Override
    public ServerNode getServer() {
        return theServer;
    }

    @Override
    public String getServerNodeId() {
        return theServer.getNodeId();
    }
}
