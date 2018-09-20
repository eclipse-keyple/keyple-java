/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.websocket.demoCSM;


import org.eclipse.keyple.plugin.remote_se.nse.NativeSeRemoteService;
import org.eclipse.keyple.plugin.remote_se.transport.ConnectionCb;
import org.eclipse.keyple.plugin.remote_se.transport.TransportNode;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclise.keyple.example.stub.calypso.CSMStubSE;
import org.eclise.keyple.example.stub.calypso.HoplinkStubSE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class wskNativeCSM {

    private static final Logger logger = LoggerFactory.getLogger(wskNativeCSM.class);


    // physical reader
    StubReader localReader;

    void boot() throws URISyntaxException, UnknownHostException {

        logger.info("**********************************************************");
        logger.info("Connect a Native Reader through NativeSeRemoteService     ");
        logger.info("**********************************************************");

        logger.info("Get SeProxy services");
        SeProxyService seProxyService = SeProxyService.getInstance();


        logger.info("Create Local StubPlugin");
        StubPlugin stubPlugin = StubPlugin.getInstance();
        SortedSet<ReaderPlugin> plugins = new TreeSet<ReaderPlugin>();
        plugins.add(stubPlugin);
        seProxyService.setPlugins(plugins);
        localReader = stubPlugin.plugStubReader("stubPO");
        localReader.insertSe(new CSMStubSE());

        logger.info("Connect Reader : ", localReader.getName());

        logger.info("Bing Web Socket Server to NatiseSeRemoteServer");
        final NativeSeRemoteService nseService = new NativeSeRemoteService();



        logger.info("************************");
        logger.info("Boot Server Network     ");
        logger.info("************************");


        logger.info("Init Web Socket Server");
        Integer port = 8000;
        String END_POINT = "/remote-se";
        String URL = "0.0.0.0";
        InetSocketAddress inet = new InetSocketAddress(Inet4Address.getByName(URL), port);

        final WskServer wskServer = new WskServer(inet, new ConnectionCb() {
            @Override
            public void onConnection(Object connection) {
                logger.info("Connect reader");

                Map<String, Object> options = new HashMap<String, Object>();
                options.put("isAsync", true);
                nseService.connectReader(localReader, options);

            }
        });

        nseService.bind((TransportNode) wskServer);

        logger.info("Starting Server on http://{}:{}{}", inet.getHostName(), inet.getPort(),
                END_POINT);
        wskServer.run();

    }




    public static void main(String[] args) throws Exception {


        wskNativeCSM client = new wskNativeCSM();
        client.boot();
        Thread.sleep(5000);

    }


}
