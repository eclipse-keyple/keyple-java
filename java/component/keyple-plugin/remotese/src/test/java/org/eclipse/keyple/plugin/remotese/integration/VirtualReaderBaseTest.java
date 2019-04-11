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
package org.eclipse.keyple.plugin.remotese.integration;


import org.eclipse.keyple.plugin.remotese.nativese.SlaveAPI;
import org.eclipse.keyple.plugin.remotese.pluginse.MasterAPI;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReader;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportFactory;
import org.eclipse.keyple.plugin.remotese.transport.impl.java.LocalTransportFactory;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubProtocolSetting;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.junit.*;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test Virtual Reader Service with stub plugin and hoplink SE
 */
public class VirtualReaderBaseTest {

    @Rule
    public TestName name = new TestName();

    private static final Logger logger = LoggerFactory.getLogger(VirtualReaderBaseTest.class);

    // Real objects
    private TransportFactory factory;
    private SlaveAPI slaveAPI;
    StubReader nativeReader;
    VirtualReader virtualReader;

    final String NATIVE_READER_NAME = "testStubReader";
    final String CLIENT_NODE_ID = "testClientNodeId";
    final String SERVER_NODE_ID = "testServerNodeId";

    // Spy Object
    MasterAPI masterAPI;

    protected void initKeypleServices() throws Exception {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName());
        logger.info("------------------------------");

        // assert that there is no stub readers plugged already
        Assert.assertEquals(0, StubPlugin.getInstance().getReaders().size());

        logger.info("*** Init LocalTransportFactory");
        // use a local transport factory for testing purposes (only java calls between client and
        // server). Only one client and one server bound together.
        factory = new LocalTransportFactory(SERVER_NODE_ID);

        logger.info("*** Bind Master Services");
        // bind Master services to server
        masterAPI = Integration.bindMaster(factory.getServer());

        logger.info("*** Bind Slave Services");
        // bind Slave services to client
        slaveAPI = Integration.bindSlave(factory.getClient(CLIENT_NODE_ID), SERVER_NODE_ID);



    }

    protected void clearStubpluginReaders() throws Exception {

        logger.info("Cleaning of the stub plugin");

        StubPlugin stubPlugin = StubPlugin.getInstance();

        // if nativeReader was initialized during test, unplug it
        if (nativeReader != null) {
            stubPlugin.unplugStubReader(nativeReader.getName(), true);
            nativeReader.clearObservers();
        }



        // stubPlugin.removeObserver(stubPluginObserver);

        // Thread.sleep(500);

        logger.info("End of cleaning of the stub plugin");
    }



    protected StubReader connectStubReader(String readerName, String nodeId) throws Exception {
        // configure native reader
        StubReader nativeReader = (StubReader) Integration.createStubReader(readerName);
        nativeReader.addSeProtocolSetting(
                new SeProtocolSetting(StubProtocolSetting.SETTING_PROTOCOL_ISO14443_4));
        this.slaveAPI.connectReader(nativeReader);
        return nativeReader;
    }

    protected void disconnectStubReader(String sessionId, String nativeReaderName, String nodeId)
            throws Exception {
        this.slaveAPI.disconnectReader(sessionId, nativeReaderName);
    }

    protected VirtualReader getVirtualReader() throws Exception {
        Assert.assertEquals(1, this.masterAPI.getPlugin().getReaders().size());
        return (VirtualReader) this.masterAPI.getPlugin().getReaders().first();
    }

}
