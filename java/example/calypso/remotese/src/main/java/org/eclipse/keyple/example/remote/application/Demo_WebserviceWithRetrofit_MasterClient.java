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

import org.eclipse.keyple.example.calypso.common.stub.se.StubCalypsoClassic;
import org.eclipse.keyple.example.remote.transport.wspolling.client_retrofit.WsPollingRetrofitFactory;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportFactory;

/**
 * Demo Web Service with Retrofit http client library (Android friendly) The master device uses the
 * webservice client whereas the slave device uses the webservice server
 */
public class Demo_WebserviceWithRetrofit_MasterClient {

    public static void main(String[] args) throws Exception {


        final String CLIENT_NODE_ID = "Demo_WebserviceWithRetrofit_MasterClient1";
        final String SERVER_NODE_ID = "Demo_WebserviceWithRetrofit_MasterClientServer1";


        // Create a HTTP Web Polling factory with a retrofitClient
        TransportFactory factory = new WsPollingRetrofitFactory(SERVER_NODE_ID);


        // Launch the Server thread
        // Server is slave
        Demo_Slave slave = new Demo_Slave(factory, true, factory.getServerNodeId(), CLIENT_NODE_ID);

        Thread.sleep(1000);

        // Launch the client
        // Client is Master
        Demo_Master master = new Demo_Master(factory, false, CLIENT_NODE_ID);
        master.boot();


        // execute slave scenario
        slave.insertSE(new StubCalypsoClassic(), true);
    }
}
