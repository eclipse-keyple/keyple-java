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
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginInstantiationException;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.example.common.calypso.stub.StubCalypsoClassic;
import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.nativese.INativeReaderService;
import org.eclipse.keyple.plugin.remotese.nativese.SlaveAPI;
import org.eclipse.keyple.plugin.remotese.transport.DtoNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.ClientNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.ServerNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportFactory;
import org.eclipse.keyple.plugin.stub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo_Slave is where slave readers are physically located, it connects one native reader to the
 * master to delegate control of it
 */
public class Demo_Slave {

    private static final Logger logger = LoggerFactory.getLogger(Demo_Slave.class);

    // physical reader, in this case a StubReader
    private StubReader localReader;

    // DtoNode used as to send and receive KeypleDto to Master
    private DtoNode node;

    // SlaveAPI, used to connect PoReader and disconnect readers
    private SlaveAPI slaveAPI;

    private String nativeReaderName;

    static public String STUB_SLAVE = "stubSlave";


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


        nativeReaderName = "STUB_READER" + slaveNodeId;

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

                initPoReader();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {


            // Slave is client, connectPoReader to Master Server
            node = transportFactory.getClient(slaveNodeId);

            // slave client uses its clientid to connect to server
            // slaveNodeId = node.getNodeId();

            ((ClientNode) node).connect(new ClientNode.ConnectCallback() {
                @Override
                public void onConnectSuccess() {
                    logger.info("{} onConnectSuccess ", slaveNodeId);
                }

                @Override
                public void onConnectFailure() {}
            });
            // if slave is client, master is the configured server
            slaveAPI = new SlaveAPI(SeProxyService.getInstance(), node,
                    ((ClientNode) node).getServerNodeId());

            initPoReader();

        }

    }

    /**
     * Creates and configures a {@link StubReaderImpl} for the PO
     *
     * @throws KeypleReaderException
     * @throws InterruptedException
     */
    final private void initPoReader() {

        try {
            logger.info("{} Boot DemoSlave LocalReader ", node.getNodeId());

            logger.info("{} Create Local StubPlugin", node.getNodeId());

            /* Get the instance of the SeProxyService (Singleton pattern) */
            SeProxyService seProxyService = SeProxyService.getInstance();

            /* Assign PcscPlugin to the SeProxyService */
            seProxyService.registerPlugin(new StubPluginFactory(STUB_SLAVE));

            ReaderPlugin stubPlugin = seProxyService.getPlugin(STUB_SLAVE);

            ObservablePlugin.PluginObserver observer = new ObservablePlugin.PluginObserver() {
                @Override
                public void update(PluginEvent event) {
                    logger.info("{} Update - pluginEvent from inline observer {}", node.getNodeId(),
                            event);
                }
            };

            // add observer to have the reader management done by the monitoring thread
            ((ObservablePlugin) stubPlugin).addObserver(observer);
            Thread.sleep(100);

            ((StubPlugin) stubPlugin).plugStubReader(nativeReaderName, true);

            Thread.sleep(1000);

            // get the created proxy reader
            localReader = (StubReader) stubPlugin.getReader(nativeReaderName);

            localReader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4,
                    StubProtocolSetting.STUB_PROTOCOL_SETTING
                            .get(SeCommonProtocols.PROTOCOL_ISO14443_4));

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
        } catch (KeyplePluginNotFoundException e) {
            e.printStackTrace();
        } catch (KeyplePluginInstantiationException e) {
            e.printStackTrace();
        }


    }


    /**
     * Creates a {@link StubReaderImpl} and connects it to the Master terminal via the
     * {@link INativeReaderService}
     * 
     * @throws KeypleReaderException
     * @throws InterruptedException
     */
    public String connectPoReader() throws KeypleReaderException {

        // connect a reader to Remote Plugin
        logger.info(
                "*****************************************************************************");
        logger.info("{} Connect remotely the Native Reader ", node.getNodeId());
        return slaveAPI.connectReader(localReader);

    }

    public void insertCalypsoSE() {
        logger.info(
                "*****************************************************************************");
        logger.info("{} Start DEMO - insert Calypso  ", node.getNodeId());

        // logger.info("{} Insert CalypsoSE into Local StubReader",node.getNodeId());

        /* Create 'virtual' Calypso PO */
        StubSecureElement calypsoStubSe = new StubCalypsoClassic();

        localReader.insertSe(calypsoStubSe);
    }

    public void insertStubSe(StubSecureElement se) {
        logger.info(
                "*****************************************************************************");
        logger.info("{} Start DEMO - insert HoplinkSE  ", node.getNodeId());

        // logger.info("{} Insert HoplinkStubSE into Local StubReader",node.getNodeId());
        localReader.insertSe(se);
    }

    public void removeSe() {

        logger.info("{} remove SE ", node.getNodeId());
        logger.info(
                "*****************************************************************************");

        localReader.removeSe();

    }

    public void disconnect(String sessionId, String nativeReaderName)
            throws KeypleReaderException, KeypleRemoteException {

        logger.info("{} Disconnect native reader ", node.getNodeId());
        logger.info(
                "*****************************************************************************");

        slaveAPI.disconnectReader(sessionId, localReader.getName());
    }

    public void executeScenario(final StubSecureElement se, final Boolean killAtEnd)
            throws KeypleReaderNotFoundException, InterruptedException, KeypleReaderException,
            KeypleRemoteException {
        // logger.info("------------------------");
        logger.info("{} Connect Reader to Master", node.getNodeId());
        // logger.info("------------------------");

        Thread.sleep(2000);
        String sessionId = this.connectPoReader();
        // logger.info("--------------------------------------------------");
        logger.info("{} Session created on server {}", node.getNodeId(), sessionId);
        // logger.info("Wait 2 seconds, then insert SE");
        // logger.info("--------------------------------------------------");
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
