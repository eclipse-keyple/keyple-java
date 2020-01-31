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
package org.eclipse.keyple.example.remote.application.multi;

import java.util.Random;
import org.eclipse.keyple.example.common.calypso.stub.StubCalypsoClassic;
import org.eclipse.keyple.example.remote.application.MasterNodeController;
import org.eclipse.keyple.example.remote.application.SlaveNodeController;
import org.eclipse.keyple.example.remote.transport.wspolling.client_retrofit.WsPollingFactory;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportFactory;

/**
 * Demo Web Service with Retrofit http client library (Android friendly) The master device uses the
 * websocket master whereas the slave device uses the websocket client
 */
public class Demo_Webservice_MasterServer_AllinOne {


    public static void main(String[] args) throws Exception {

        final String SERVER_NODE_ID = "RSEServer1";
        final String CLIENT_NODE_ID = "RSEClient1";
        final String CLIENT_NODE_ID2 = "RSEClient2";



        final Integer port = 8000 + new Random().nextInt((100) + 1);
        final String hostname = "0.0.0.0";
        final String protocol = "http://";

        // Create the procotol factory
        TransportFactory factory = new WsPollingFactory(SERVER_NODE_ID, protocol, hostname, port);

        /*
         * Launch Server (master)
         */
        MasterNodeController master = new MasterNodeController(factory, true, null);
        master.boot();


        Thread.sleep(1000);

        /*
         * Launch Client 1 (slave)
         */
        SlaveNodeController slave =
                new SlaveNodeController(factory, false, CLIENT_NODE_ID, SERVER_NODE_ID);


        Thread.sleep(1000);

        /*
         * Launch Client 2 (slave)
         */
        SlaveNodeController slave2 =
                new SlaveNodeController(factory, false, CLIENT_NODE_ID2, SERVER_NODE_ID);

        Thread.sleep(2000);

        for (int i = 0; i < 10; i++) {
            // execute Calypso Transaction Scenario
            slave.executeScenario(new StubCalypsoClassic(), false);
            slave2.executeScenario(new StubCalypsoClassic(), false);
            Thread.sleep(10000);
        }
    }
}
