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

import org.eclipse.keyple.example.remote.application.MasterNodeController;
import org.eclipse.keyple.example.remote.transport.wspolling.client_retrofit.WsPollingFactory;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportFactory;

/**
 * Demo Web Service with Retrofit http client library (Android friendly) The master device uses the
 * websocket master whereas the slave device uses the websocket client
 */
public class Demo_Webservice_MasterServer_Server {


    static public final Integer port = 8888;
    static public final String hostname = "0.0.0.0";
    static public final String protocol = "http://";


    public static void main(String[] args) throws Exception {

        final String SERVER_NODE_ID = "RSEServer1";


        // Create the procotol factory
        TransportFactory factory = new WsPollingFactory(SERVER_NODE_ID, protocol, hostname, port);

        // Launch the Server thread
        // Server is Master
        MasterNodeController master = new MasterNodeController(factory, true, null);
        master.boot();

    }
}
