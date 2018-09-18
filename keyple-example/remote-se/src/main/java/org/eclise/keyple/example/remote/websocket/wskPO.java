/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.websocket;


import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclise.keyple.example.remote.websocket.websocket.WskClient;
import org.eclise.keyple.example.stub.calypso.HoplinkStubSE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.SortedSet;
import java.util.TreeSet;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class wskPO {

    private static final Logger logger = LoggerFactory.getLogger(wskPO.class);

    private static String ENDPOINT_URL = "http://localhost:8000/remote-se";

    // physical reader
    StubReader localReader;

    void boot() {
        logger.info("************************");
        logger.info("Boot Client LocalReader ");
        logger.info("************************");

        //get seProxyService
        SeProxyService seProxyService = SeProxyService.getInstance();

        logger.info("Create Local StubPlugin");
        StubPlugin stubPlugin = StubPlugin.getInstance();
        SortedSet<ReaderPlugin> plugins = new TreeSet<ReaderPlugin>();
        plugins.add(stubPlugin);
        seProxyService.setPlugins(plugins);
        localReader = stubPlugin.plugStubReader("stubPO");

        try {
            //todo configure remote service with web service nse
            WskClient wskClient = new WskClient(new URI(ENDPOINT_URL));
            wskClient.connect();




        }  catch (URISyntaxException e) {
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


        wskPO client = new wskPO();
        client.boot();
        client.demo();


    }



}
