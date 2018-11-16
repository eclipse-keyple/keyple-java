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

import java.io.IOException;
import org.eclipse.keyple.example.remote.wspolling.server.WsPServer;
import org.eclipse.keyple.plugin.remotese.transport.KeypleDtoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private final Integer PORT = 8004;
    private final String END_POINT = "/keypleDTO";
    private final String POLLING_END_POINT = "/polling";
    private final String URL = "0.0.0.0";



    private void boot() throws IOException {


        logger.info("*************************************");
        logger.info("Start Polling Webservice server      ");
        logger.info("*************************************");

        WsPServer server = new WsPServer(URL, PORT, END_POINT, POLLING_END_POINT, "server");
        server.start();


        for (int i = 0; i < 10; i++) {

            try {
                Thread.sleep(17000);

                // send message to client by polling (suppose that a client is polling)
                server.update(KeypleDtoHelper.ACK());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }


    public static void main(String[] args) throws Exception {

        Server server = new Server();
        server.boot();
    }
}
