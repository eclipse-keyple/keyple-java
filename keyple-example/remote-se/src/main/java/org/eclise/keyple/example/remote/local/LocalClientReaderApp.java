/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.local;

import java.io.IOException;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclise.keyple.example.remote.server.transport.RseClient;
import org.eclise.keyple.example.remote.server.transport.NseAPI;
import org.eclise.keyple.example.remote.server.transport.local.LocalServer;
import org.eclise.keyple.example.remote.server.transport.local.nse.LocalNseAPI;
import org.eclise.keyple.example.stub.calypso.HoplinkStubSE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalClientReaderApp {

    private static final Logger logger = LoggerFactory.getLogger(LocalClientReaderApp.class);

    StubReader localReader;


    void boot() {
        logger.info("************************");
        logger.info("Boot Client LocalReader ");
        logger.info("************************");

        logger.info("Create Local StubPlugin");
        // register StubPlugin with StubReader
        StubPlugin plugin = StubPlugin.getInstance();
        localReader = plugin.plugStubReader("stubPhysicalReader");

        logger.info("Connect remotely the StubPlugin to rse");
        // configure ServerConnection (ie Local, Websocket) with a configuration Bundle
        try {
            // get local rse
            LocalServer localServer = LocalServer.getInstance();

            // create a nse listener for duplex connection
            NseAPI nseAPI = new LocalNseAPI(localReader);

            // connect to rse
            RseClient RSENative = localServer.getConnection(nseAPI);

            // connect reader
            RSENative.connectReader(localReader, null);

            localReader.addObserver(RSENative);
            logger.info("OK");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void demo() {
        logger.info("************************");
        logger.info("Start DEMO - insert SE  ");
        logger.info("************************");

        logger.info("Insert HoplinkStubSE into Local StubReader");
        // insert SE
        localReader.insertSe(new HoplinkStubSE());

        logger.info("************************");
        logger.info("        remove SE       ");
        logger.info("************************");

        localReader.removeSe();

    }



}
