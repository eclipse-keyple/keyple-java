package org.eclipse.keyple.example.remote;

import org.eclipse.keyple.example.remote.common.TransportFactory;
import org.eclipse.keyple.example.remote.websocket.WskFactory;

public class WsKMasterServer {


    public static void main(String[] args) throws Exception {

        Boolean isTransmitSync = true; // is Transmit API Blocking or Not Blocking

        TransportFactory factory = new WskFactory(); // Web socket

        Boolean isMasterServer = true; // Master is the server (and Slave the Client)

        /**
         * ProtocolThreads
         */

        ProtocolThreads.startServer(isTransmitSync, isMasterServer, factory);
        Thread.sleep(1000);
        ProtocolThreads.startClient(isTransmitSync, !isMasterServer, factory);
    }
}
