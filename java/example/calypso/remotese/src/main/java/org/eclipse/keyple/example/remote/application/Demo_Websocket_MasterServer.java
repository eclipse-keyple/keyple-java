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
package org.eclipse.keyple.example.remote.application;

import org.eclipse.keyple.example.common.calypso.stub.StubCalypsoClassic;
import org.eclipse.keyple.example.remote.transport.websocket.WskFactory;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportFactory;

/**
 * Demo websocket
 *
 * The master device uses the websocket server whereas the slave device uses the websocket client
 *
 */
public class Demo_Websocket_MasterServer {

    public static void main(String[] args) throws Exception {


        final String CLIENT_NODE_ID = "WskMS1";
        final String SERVER_NODE_ID = "WskMS1Server";


        // Create the procotol factory
        // Web socket
        TransportFactory factory = new WskFactory(true, SERVER_NODE_ID);

        // Launch the Server thread
        // Server is Master
        MasterNodeController master = new MasterNodeController(factory, true, null);
        master.boot();

        Thread.sleep(1000);// wait for the server to boot

        // Launch the client thread
        // Client is Slave
        SlaveNodeController slave =
                new SlaveNodeController(factory, false, CLIENT_NODE_ID, SERVER_NODE_ID);

        // execute Calypso Transaction Scenario
        slave.executeScenario(new StubCalypsoClassic(), true);

    }
}
