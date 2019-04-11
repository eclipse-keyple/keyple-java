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
import org.eclipse.keyple.plugin.remotese.nativese.INativeReaderService;
import org.eclipse.keyple.plugin.remotese.nativese.SlaveAPI;
import org.eclipse.keyple.plugin.remotese.transport.DtoNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.ClientNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.ServerNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportFactory;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubProtocolSetting;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.plugin.stub.StubSecureElement;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo_Slave is where slave readers are physically located, it connects one native reader to the
 * master to delegate control of it
 */
class Demo_Slave {

    private static final Logger logger = LoggerFactory.getLogger(Demo_Slave.class);

    // physical reader, in this case a StubReader
    private StubReader localReader;

    // DtoNode used as to send and receive KeypleDto to Master
    private DtoNode node;

    // private String slaveNodeId;

    // NativeReaderServiceImpl, used to connectAReader and disconnect readers
    private SlaveAPI slaveAPI;

    /**
     * At startup, create the {@link DtoNode} object, either a {@link ClientNode} or a
     * {@link ServerNode}
     * 
     * @param transportFactory : factory to get the type of transport needed (websocket,
     *        webservice...)
     * @param isServer : true if a Server is wanted
     */
    public Demo_Slave(final TransportFactory transportFactory, Boolean isServer,
            final String slaveNodeId, String masterNodeId) {
        logger.info(
                "*****************************************************************************");
        logger.info("{} Create DemoSlave    ", slaveNodeId);
        logger.info(
                "*****************************************************************************");

        if (isServer) {
            // Slave is a server, start Server and wait for Master clients
            try {
                node = transportFactory.getServer();

                // slave server needs to know to which master client it should connects
                // slaveNodeId = transportFactory.getClient(slaveNodeId).getNodeId();

                // start server in a new thread
                new Thread() {
                    @Override
                    public void run() {
                        ((ServerNode) node).start();
                        logger.info("{} Waits for remote connections", slaveNodeId);
                    }
                }.start();

                // if slave is server, must specify which master to connect to
                slaveAPI = new SlaveAPI(SeProxyService.getInstance(), node, masterNodeId);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            // Slave is client, connectAReader to Master Server
            node = transportFactory.getClient(slaveNodeId);

            // slave client uses its clientid to connect to server
            // slaveNodeId = node.getNodeId();

            ((ClientNode) node).connect(new ClientNode.ConnectCallback() {
                @Override
                public void onConnectSuccess() {
                    logger.info("Client connected");
                }

                @Override
                public void onConnectFailure() {

                }
            });
            // if slave is client, master is the configured server
            slaveAPI = new SlaveAPI(SeProxyService.getInstance(), node,
                    ((ClientNode) node).getServerNodeId());

            /*
             * // start client in a new thread new Thread() {
             * 
             * @Override public void run() { } }.start();
             */


        }

    }



    /**
     * Creates a {@link StubReader} and connects it to the Master terminal via the
     * {@link INativeReaderService}
     * 
     * @throws KeypleReaderException
     * @throws InterruptedException
     */
    public String connectAReader()
            throws KeypleReaderException, InterruptedException, KeypleRemoteException {


        logger.info("{} Boot DemoSlave LocalReader ", node.getNodeId());

        logger.info("{} Create Local StubPlugin", node.getNodeId());
        StubPlugin stubPlugin = StubPlugin.getInstance();

        SeProxyService.getInstance().addPlugin(stubPlugin);

        ObservablePlugin.PluginObserver observer = new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent event) {
                logger.info("{} Update - pluginEvent from inline observer", node.getNodeId(),
                        event);
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

        // Binds node for incoming KeypleDTo
        // slaveAPI.bindDtoEndpoint(node);

        // connect a reader to Remote Plugin
        logger.info("{} Connect remotely the StubPlugin ", node.getNodeId());
        return slaveAPI.connectReader(localReader);

    }

    public void insertCalypsoSE() {
        logger.info(
                "*****************************************************************************");
        logger.info("{} Start DEMO - insert Calypso  ", node.getNodeId());
        logger.info(
                "*****************************************************************************");

        // logger.info("{} Insert CalypsoSE into Local StubReader",node.getNodeId());

        /* Create 'virtual' Calypso PO */
        StubSecureElement calypsoStubSe = new StubCalypsoClassic();

        localReader.insertSe(calypsoStubSe);
    }

    public void insertStubSe(StubSecureElement se) {
        logger.info(
                "*****************************************************************************");
        logger.info("{} Start DEMO - insert HoplinkSE  ", node.getNodeId());
        logger.info(
                "*****************************************************************************");

        // logger.info("{} Insert HoplinkStubSE into Local StubReader",node.getNodeId());
        localReader.insertSe(se);
    }

    public void removeSe() {

        logger.info(
                "*****************************************************************************");
        logger.info("{} remove SE ", node.getNodeId());
        logger.info(
                "*****************************************************************************");

        localReader.removeSe();

    }

    public void disconnect(String sessionId, String nativeReaderName)
            throws KeypleReaderException, KeypleRemoteException {

        logger.info(
                "*****************************************************************************");
        logger.info("{} Disconnect native reader ", node.getNodeId());
        logger.info(
                "*****************************************************************************");

        slaveAPI.disconnectReader(sessionId, localReader.getName());
    }

    public void insertSE(final StubSecureElement se, final Boolean killAtEnd)
            throws KeypleReaderNotFoundException, InterruptedException, KeypleReaderException,
            KeypleRemoteException {
        logger.info("------------------------");
        logger.info("{} Connect Reader to Master", node.getNodeId());
        logger.info("------------------------");

        Thread.sleep(2000);
        String sessionId = this.connectAReader();
        logger.info("--------------------------------------------------");
        logger.info("{} Session created on server {}", node.getNodeId(), sessionId);
        // logger.info("Wait 2 seconds, then insert SE");
        logger.info("--------------------------------------------------");

        // Thread.sleep(2000);

        logger.info("{} Inserting SE", node.getNodeId());
        this.insertStubSe(se);
        logger.info("{} Wait 2 seconds, then remove SE", node.getNodeId());
        Thread.sleep(2000);
        this.removeSe();
        logger.info("{} Wait 2 seconds, then disconnect reader", node.getNodeId());
        Thread.sleep(2000);
        this.disconnect(sessionId, null);

        if (killAtEnd) {
            logger.info("{} Wait 2 seconds, then shutdown jvm", node.getNodeId());
            Thread.sleep(2000);

            Runtime.getRuntime().exit(0);

        }

    }



}
