/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.webservice.demo1;


import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclise.keyple.example.remote.webservice.webservice.nse.WsRseClient;
import org.eclise.keyple.example.stub.calypso.HoplinkStubSE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class PO {

    private static final Logger logger = LoggerFactory.getLogger(PO.class);

    private static String ENDPOINT_URL = "http://localhost:8000/remote-se";

    // physical reader
    StubReader localReader;

    void boot() {
        logger.info("************************");
        logger.info("Boot Client LocalReader ");
        logger.info("************************");

        // get seProxyService
        SeProxyService seProxyService = SeProxyService.getInstance();

        logger.info("Create Local StubPlugin");
        StubPlugin stubPlugin = StubPlugin.getInstance();
        SortedSet<ReaderPlugin> plugins = new TreeSet<ReaderPlugin>();
        plugins.add(stubPlugin);
        seProxyService.setPlugins(plugins);
        localReader = stubPlugin.plugStubReader("stubPO");

        try {
            // todo configure remote service with web service nse
            WsRseClient wsClientRSEClient = new WsRseClient(ENDPOINT_URL);

            // logger.info(
            // "Register wsClientRSEClient as an observer of the local stubPlugin thus events will
            // be propagated");


            // todo connect reader to remote service
            String sessionId = wsClientRSEClient.connectReader(localReader, null);
            // todo observers is included in connect
            localReader.addObserver(wsClientRSEClient);

            logger.info("Connect remotely the StubPlugin to rse with sessionId {}", sessionId);

        } catch (IOException e) {
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

        // todo Remove SE
        // logger.info("************************");
        // logger.info(" remove SE ");
        // logger.info("************************");
        //
        // localReader.removeSe();

    }



    public static void main(String[] args) throws Exception {


        PO client = new PO();
        client.boot();
        client.demo();


    }



}
