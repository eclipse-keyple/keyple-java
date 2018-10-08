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
