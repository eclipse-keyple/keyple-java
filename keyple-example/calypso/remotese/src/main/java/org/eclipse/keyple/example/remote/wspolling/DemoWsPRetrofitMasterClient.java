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
package org.eclipse.keyple.example.remote.wspolling;

import org.eclipse.keyple.example.remote.calypso.DemoThreads;
import org.eclipse.keyple.example.remote.transport.TransportFactory;
import org.eclipse.keyple.example.remote.wspolling.client_retrofit.WsPollingRetrofitFactory;

public class DemoWsPRetrofitMasterClient {

    // blocking : works

    public static void main(String[] args) throws Exception {

        Boolean isMasterServer = false; // DemoMaster is the Client (and DemoSlave the server)

        TransportFactory factory = new WsPollingRetrofitFactory(); // HTTP Web Polling with Android
                                                                   // compatible client_retrofit
                                                                   // Library

        DemoThreads.startServer(isMasterServer, factory);
        Thread.sleep(1000);
        DemoThreads.startClient(!isMasterServer, factory);
    }
}
