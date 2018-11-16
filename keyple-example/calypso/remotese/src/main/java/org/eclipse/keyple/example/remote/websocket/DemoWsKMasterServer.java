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
package org.eclipse.keyple.example.remote.websocket;

import org.eclipse.keyple.example.remote.calypso.DemoThreads;
import org.eclipse.keyple.example.remote.transport.TransportFactory;

public class DemoWsKMasterServer {

    public static void main(String[] args) throws Exception {

        TransportFactory factory = new WskFactory(); // Web socket
        Boolean isMasterServer = true; // DemoMaster is the server (and DemoSlave the Client)

        /**
         * DemoThreads
         */

        DemoThreads.startServer(isMasterServer, factory);
        Thread.sleep(1000);
        DemoThreads.startClient(!isMasterServer, factory);
    }
}
