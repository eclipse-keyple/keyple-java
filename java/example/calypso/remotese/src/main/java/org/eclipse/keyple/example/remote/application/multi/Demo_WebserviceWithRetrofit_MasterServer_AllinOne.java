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
import org.eclipse.keyple.example.calypso.common.stub.se.StubCalypsoClassic;
import org.eclipse.keyple.example.remote.application.Demo_Master;
import org.eclipse.keyple.example.remote.application.Demo_Slave;
import org.eclipse.keyple.example.remote.transport.wspolling.client_retrofit.WsPollingRetrofitFactory;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportFactory;

/**
 * Demo Web Service with Retrofit http client library (Android friendly) The master device uses the
 * websocket master whereas the slave device uses the websocket client
 */
public class Demo_WebserviceWithRetrofit_MasterServer_AllinOne {


    public static void main(String[] args) throws Exception {

        final String SERVER_NODE_ID = "RSEServer1";
        final String CLIENT_NODE_ID = "RSEClient1";
        final String CLIENT_NODE_ID2 = "RSEClient2";



        final Integer port = 8000 + new Random().nextInt((100) + 1);
        final String hostname = "0.0.0.0";
        final String protocol = "http://";

        // Create the procotol factory
        TransportFactory factory =
                new WsPollingRetrofitFactory(SERVER_NODE_ID, protocol, hostname, port);

        /*
         * Launch Server (master)
         */
        Demo_Master master = new Demo_Master(factory, true, null);
        master.boot();


        Thread.sleep(1000);

        /*
         * Launch Client 1 (slave)
         */
        Demo_Slave slave = new Demo_Slave(factory, false, CLIENT_NODE_ID, SERVER_NODE_ID);


        Thread.sleep(1000);

        /*
         * Launch Client 2 (slave)
         */
        Demo_Slave slave2 = new Demo_Slave(factory, false, CLIENT_NODE_ID2, SERVER_NODE_ID);

        Thread.sleep(2000);

        for (int i = 0; i < 10; i++) {
            // execute Calypso Transaction Scenario
            slave.executeScenario(new StubCalypsoClassic(), false);
            slave2.executeScenario(new StubCalypsoClassic(), false);
            Thread.sleep(10000);
        }
    }
}
