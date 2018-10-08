/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.remote;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import org.eclipse.keyple.example.pc.calypso.stub.se.StubHoplink;
import org.eclipse.keyple.example.remote.common.ClientNode;
import org.eclipse.keyple.example.remote.common.ServerNode;
import org.eclipse.keyple.example.remote.common.TransportFactory;
import org.eclipse.keyple.plugin.remote_se.transport.DtoSender;
import org.eclipse.keyple.plugin.remote_se.transport.TransportNode;
import org.eclipse.keyple.plugin.remote_se.nse.NativeSeRemoteService;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slave {

    private static final Logger logger = LoggerFactory.getLogger(Slave.class);

    // physical reader
    StubReader localReader;
    private TransportNode node;

    public Slave(TransportFactory transportFactory, Boolean isServer) {
        logger.info("*******************");
        logger.info("Create Slave    ");
        logger.info("*******************");

        if (isServer) {
            try {
                node = transportFactory.getServer(false);
                // start server in a new thread
                new Thread() {
                    @Override
                    public void run() {
                        ((ServerNode) node).start();
                        logger.info("Waits for remote connections");
                    }
                }.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            node = transportFactory.getClient(false);
            ((ClientNode) node).connect();
        }
    }

    public void connect() throws KeypleReaderNotFoundException, InterruptedException, IOException {


        logger.info("Boot Slave LocalReader ");
        String nodeId = "node1";

        // get seProxyService
        SeProxyService seProxyService = SeProxyService.getInstance();

        logger.info("Create Local StubPlugin");
        StubPlugin stubPlugin = StubPlugin.getInstance();
        SortedSet<ReaderPlugin> plugins = SeProxyService.getInstance().getPlugins();
        plugins.add(stubPlugin);
        seProxyService.setPlugins(plugins);
        stubPlugin.plugStubReader("stubClientSlave");

        Thread.sleep(1000);

        // get the created proxy reader
        localReader = (StubReader) stubPlugin.getReader("stubClientSlave");

        NativeSeRemoteService seRemoteService = new NativeSeRemoteService(node);//ougoing traffic
        seRemoteService.bindDtoEndpoint(node);//incoming traffic

        Map<String, Object> options = new HashMap<String, Object>();
        options.put("isAsync", true);
        seRemoteService.connectReader(nodeId, localReader, options);

        logger.info("Connect remotely the StubPlugin ");
    }

    public void insertSe() {
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



}
