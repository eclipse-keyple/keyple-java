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
package org.eclipse.keyple.example.remote.application;

import java.io.IOException;
import org.eclipse.keyple.example.calypso.common.stub.se.StubCalypsoClassic;
import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.nativese.NativeReaderServiceImpl;
import org.eclipse.keyple.plugin.remotese.transport.factory.ClientNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.ServerNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportFactory;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportNode;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubProtocolSetting;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.plugin.stub.StubSecureElement;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DemoSlave is where slave readers are physically located It connects one native reader to the
 * master to delegate control of it
 */
class Demo_Slave {

    private static final Logger logger = LoggerFactory.getLogger(Demo_Slave.class);

    // physical reader, in this case a StubReader
    private StubReader localReader;

    // TransportNode used as to send and receive KeypleDto to Master
    private TransportNode node;

    private String clientNodeId;

    // NativeReaderServiceImpl, used to connectAReader and disconnect readers
    private NativeReaderServiceImpl nativeReaderService;

    /**
     * At startup, create the {@link TransportNode} object, either a {@link ClientNode} or a
     * {@link ServerNode}
     * 
     * @param transportFactory : factory to get the type of transport needed (websocket,
     *        webservice...)
     * @param isServer : true if a Server is wanted
     */
    public Demo_Slave(TransportFactory transportFactory, Boolean isServer) {
        logger.info("*******************");
        logger.info("Create DemoSlave    ");
        logger.info("*******************");

        if (isServer) {
            // Slave is a server, start Server and wait for Master clients
            try {
                node = transportFactory.getServer();

                // slave server needs to know to which master client it should connects
                clientNodeId = transportFactory.getClient().getNodeId();

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
            // Slave is client, connectAReader to Master Server
            node = transportFactory.getClient();

            // slave client uses its clientid to connect to server
            clientNodeId = node.getNodeId();

            ((ClientNode) node).connect(new ClientNode.ConnectCallback() {
                @Override
                public void onConnectSuccess() {
                    logger.info("Client connected");
                }

                @Override
                public void onConnectFailure() {

                }
            });
        }
    }

    /**
     * Creates a {@link StubReader} and connects it to the Master terminal via the
     * {@link org.eclipse.keyple.plugin.remotese.nativese.NativeReaderService}
     * 
     * @throws KeypleReaderException
     * @throws InterruptedException
     */
    public String connectAReader()
            throws KeypleReaderException, InterruptedException, KeypleRemoteException {


        logger.info("Boot DemoSlave LocalReader ");

        logger.info("Create Local StubPlugin");
        StubPlugin stubPlugin = StubPlugin.getInstance();

        SeProxyService.getInstance().addPlugin(stubPlugin);

        ObservablePlugin.PluginObserver observer = new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent event) {
                logger.info("Update - pluginEvent from inline observer", event);
            }
        };

        // add observer to have the reader management done by the monitoring thread
        stubPlugin.addObserver(observer);

        Thread.sleep(100);

        stubPlugin.plugStubReader("stubClientSlave", true);

        Thread.sleep(1000);

        // get the created proxy reader
        localReader = (StubReader) stubPlugin.getReader("stubClientSlave");

        localReader.addSeProtocolSetting(
                new SeProtocolSetting(StubProtocolSetting.SETTING_PROTOCOL_ISO14443_4));

        // Binds node for outgoing KeypleDto
        nativeReaderService = new NativeReaderServiceImpl(node);

        // Binds node for incoming KeypleDTo
        nativeReaderService.bindDtoEndpoint(node);

        // connect a reader to Remote Plugin
        logger.info("Connect remotely the StubPlugin ");
        return nativeReaderService.connectReader(localReader, clientNodeId);

    }

    public void insertSe() {
        logger.info("************************");
        logger.info("Start DEMO - insert SE  ");
        logger.info("************************");

        logger.info("Insert HoplinkStubSE into Local StubReader");

        /* Create 'virtual' Calypso PO */
        StubSecureElement calypsoStubSe = new StubCalypsoClassic();

        localReader.insertSe(calypsoStubSe);


    }

    public void removeSe() {

        logger.info("************************");
        logger.info(" remove SE ");
        logger.info("************************");

        localReader.removeSe();

    }

    public void disconnect(String sessionId, String nativeReaderName)
            throws KeypleReaderException, KeypleRemoteException {

        logger.info("*************************");
        logger.info("Disconnect native reader ");
        logger.info("*************************");

        nativeReaderService.disconnectReader(sessionId, localReader.getName(), clientNodeId);
    }



}
