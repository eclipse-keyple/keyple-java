/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.webservice.demoPO;


import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.keyple.example.pc.calypso.stub.se.StubHoplink;
import org.eclipse.keyple.plugin.remote_se.nse.NativeSeRemoteService;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclise.keyple.example.remote.webservice.WsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class WsPO {

    private static final Logger logger = LoggerFactory.getLogger(WsPO.class);

    private static String ENDPOINT_URL = "http://localhost:8007/keypleDTO";

    // physical reader
    StubReader localReader;

    void boot() {
        logger.info("************************");
        logger.info("Create Webservice Client");
        logger.info("************************");

        WsClient ws = new WsClient(ENDPOINT_URL);

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


        NativeSeRemoteService seRemoteService = new NativeSeRemoteService();
        seRemoteService.bind(ws);

        Map<String, Object> options = new HashMap<String, Object>();
        options.put("isAsync", true);

        seRemoteService.connectReader(localReader, options);

        logger.info("Connect remotely the StubPlugin ");


    }

    void demo() {
        logger.info("************************");
        logger.info("Start DEMO - insert SE  ");
        logger.info("************************");

        logger.info("Insert HoplinkStubSE into Local StubReader");
        // insert SE
        localReader.insertSe(new StubHoplink());

        // todo Remove SE
        // logger.info("************************");
        // logger.info(" remove SE ");
        // logger.info("************************");
        //
        // localReader.removeSe();

    }



    public static void main(String[] args) throws Exception {


        WsPO client = new WsPO();
        client.boot();
        client.demo();


    }



}
