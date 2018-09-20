/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.websocket.demoPO;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import org.eclipse.keyple.plugin.remote_se.nse.NativeSeRemoteService;
import org.eclipse.keyple.plugin.remote_se.transport.TransportNode;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclise.keyple.example.stub.calypso.HoplinkStubSE;
import org.java_websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class wskPO {

    private static final Logger logger = LoggerFactory.getLogger(wskPO.class);

    private static String ENDPOINT_URL = "http://localhost:8000/remote-se";

    // physical reader
    StubReader localReader;

    void boot() throws URISyntaxException {
        logger.info("************************");
        logger.info("Boot Client Network     ");
        logger.info("************************");


        WebSocketClient wskClient = new WskClient(new URI(ENDPOINT_URL));
        wskClient.connect();



        logger.info("**********************************************************");
        logger.info("Connect a Native Reader through NativeSeRemoteService     ");
        logger.info("**********************************************************");

        logger.info("Get SeProxy services");
        SeProxyService seProxyService = SeProxyService.getInstance();
        NativeSeRemoteService nseService = new NativeSeRemoteService();
        nseService.bind((TransportNode) wskClient);


        logger.info("Create Local StubPlugin");
        StubPlugin stubPlugin = StubPlugin.getInstance();
        SortedSet<ReaderPlugin> plugins = new TreeSet<ReaderPlugin>();
        plugins.add(stubPlugin);
        seProxyService.setPlugins(plugins);
        localReader = stubPlugin.plugStubReader("stubPO");

        logger.info("Connect Reader : ", localReader.getName());

        Map<String, Object> options = new HashMap<String, Object>();
        options.put("isAsync", true);
        options.put("transmitUrl", ENDPOINT_URL);

        nseService.connectReader(localReader, options);


    }

    void insertPO() {
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
        Thread.sleep(5000);
        client.insertPO();


    }



}
