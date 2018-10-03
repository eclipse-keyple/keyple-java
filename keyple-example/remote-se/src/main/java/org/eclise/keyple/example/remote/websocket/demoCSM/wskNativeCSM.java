/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.websocket.demoCSM;


import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.keyple.example.pc.calypso.stub.se.StubHoplink;
import org.eclipse.keyple.plugin.remote_se.nse.NativeSeRemoteService;
import org.eclipse.keyple.plugin.remote_se.transport.TransportNode;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclise.keyple.example.remote.websocket.ConnectionCb;
import org.eclise.keyple.example.remote.websocket.WskServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class wskNativeCSM {

    private static final Logger logger = LoggerFactory.getLogger(wskNativeCSM.class);

    StubReader localReader;
    // physical reader

    void boot() throws URISyntaxException, UnknownHostException, InterruptedException,
            KeypleReaderNotFoundException {

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
        stubPlugin.plugStubReader("stubCSM");

        Thread.sleep(1000);

        // get the created proxy reader
        localReader = (StubReader) stubPlugin.getReader("stubCSM");
        logger.info("Connect Reader : ", localReader.getName());

        // localReader.insertSe(new CSMStubSE());
        localReader.insertSe(new StubHoplink());// should be a csm

        Thread.sleep(1000);


        logger.info("Bind Web Socket Master to NatiseSeRemoteServer");
        final NativeSeRemoteService nseService = new NativeSeRemoteService();



        logger.info("************************");
        logger.info("Boot Master Network     ");
        logger.info("************************");


        logger.info("Init Web Socket Master");
        Integer port = 8002;
        String END_POINT = "/remote-se";
        String URL = "0.0.0.0";
        InetSocketAddress inet = new InetSocketAddress(Inet4Address.getByName(URL), port);
        Boolean isSlave = true;

        final WskServer wskServer = new WskServer(inet, new ConnectionCb() {
            @Override
            public void onConnection(Object connection) {
                logger.info("Connect reader");

                Map<String, Object> options = new HashMap<String, Object>();
                options.put("isAsync", true);
                nseService.connectReader(localReader, options);

            }
        }, isSlave);

        nseService.bind((TransportNode) wskServer);

        logger.info("Starting Master on http://{}:{}{}", inet.getHostName(), inet.getPort(),
                END_POINT);
        wskServer.run();

    }



    public static void main(String[] args) throws Exception {

        wskNativeCSM client = new wskNativeCSM();
        client.boot();
        Thread.sleep(5000);

    }


}
