/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.remote;

import org.eclipse.keyple.example.remote.common.TransportFactory;
import org.eclipse.keyple.example.remote.wspolling.WsPollingFactory;

public class WsPMasterClient {


    public static void main(String[] args) throws Exception {

        Boolean isTransmitSync = true; // is Transmit API Blocking or Not Blocking

        TransportFactory factory = new WsPollingFactory(); // HTTP Web Polling

        Boolean isMasterServer = false; // Master is the Client (and Slave the server)

        /**
         * ProtocolThreads
         */

        ProtocolThreads.startServer(isTransmitSync, isMasterServer, factory);
        Thread.sleep(1000);
        ProtocolThreads.startClient(isTransmitSync, !isMasterServer, factory);
    }
}
