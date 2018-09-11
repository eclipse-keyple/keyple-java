/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.client.webservice;


import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclise.keyple.example.remote.server.transport.async.webservice.client.WsRSEClient;
import org.eclise.keyple.example.stub.calypso.HoplinkStubSE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class WSClientReaderApp {

    private static final Logger logger = LoggerFactory.getLogger(WSClientReaderApp.class);

    private static String ENDPOINT_URL = "http://localhost:8000/remote-se";

    //physical reader
    StubReader localReader;

    void boot() {
        logger.info("************************");
        logger.info("Boot Client LocalReader ");
        logger.info("************************");

        logger.info("Create Local StubPlugin");
        StubPlugin plugin = StubPlugin.getInstance();
        localReader = plugin.plugStubReader("stubPhysicalReader");

        // configure web service client
        WsRSEClient wsRSEClient = new WsRSEClient(ENDPOINT_URL);

        logger.info("Register wsRSEClient as an observer of the local plugin thus events will be propagated");
        localReader.addObserver(wsRSEClient);

        try{
            //connect physical local reader to RSE plugin
            String sessionId = wsRSEClient.connectReader(localReader);
            logger.info("Connect remotely the StubPlugin to server with sessionId {}", sessionId);
        }catch (IOException e){
            logger.error(e.getMessage());
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

        //todo Remove SE
        //logger.info("************************");
        //logger.info("        remove SE       ");
        //logger.info("************************");
        //
        //localReader.removeSe();

    }




    public static void main(String[] args) throws Exception {


        WSClientReaderApp client = new WSClientReaderApp();
        client.boot();
        client.demo();


    }



}
