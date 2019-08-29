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


import java.util.Set;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.plugin.remotese.nativese.SlaveAPI;
import org.eclipse.keyple.plugin.remotese.pluginse.MasterAPI;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReader;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportFactory;
import org.eclipse.keyple.plugin.remotese.transport.impl.java.LocalTransportFactory;
import org.eclipse.keyple.plugin.stub.*;
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
    protected TransportFactory factory;
    protected StubReader nativeReader;

    protected final String NATIVE_READER_NAME = "testStubReader";
    protected final String CLIENT_NODE_ID = "testClientNodeId";
    protected final String SERVER_NODE_ID = "testServerNodeId";

    protected SeProxyService seProxyService = SeProxyService.getInstance();

    final String REMOTE_SE_PLUGIN_NAME = "remoteseplugin1";


    // Spy Object
    protected MasterAPI masterAPI;
    // Spy Object
    protected SlaveAPI slaveAPI;


    protected void initKeypleServices() throws Exception {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName());
        logger.info("------------------------------");

        Assert.assertEquals(0, SeProxyService.getInstance().getPlugins().size());

        /*
         * Register Stub Plugin
         */
        seProxyService.registerPlugin(new StubPluginFactory());
        StubPlugin stubPlugin = (StubPlugin) seProxyService.getPlugin(StubPlugin.PLUGIN_NAME);

        // assert that there is no stub readers plugged already
        Assert.assertEquals(0, stubPlugin.getReaders().size());

        logger.info("*** Init LocalTransportFactory");
        // use a local transport factory for testing purposes (only java calls between client and
        // server). Only one client and one server bound together.
        factory = new LocalTransportFactory(SERVER_NODE_ID);

        logger.info("*** Bind Master Services");

        // bind Master services to server
        masterAPI = Integration.bindMasterSpy(factory.getServer(), REMOTE_SE_PLUGIN_NAME);

        logger.info("*** Bind Slave Services");
        // bind Slave services to client
        slaveAPI = Integration.bindSlaveSpy(factory.getClient(CLIENT_NODE_ID), SERVER_NODE_ID);

    }

    protected void unregisterPlugins() {
        seProxyService.unregisterPlugin(StubPlugin.PLUGIN_NAME);
        seProxyService.unregisterPlugin(StubPoolPlugin.PLUGIN_NAME);
        seProxyService.unregisterPlugin(REMOTE_SE_PLUGIN_NAME);
    }


    @Deprecated
    protected void clearStubpluginNativeReader() throws Exception {
        logger.info("Remove nativeReader from stub plugin");
        StubPlugin stubPlugin =
                (StubPlugin) SeProxyService.getInstance().getPlugin(StubPlugin.PLUGIN_NAME);

        // if nativeReader was initialized during test, unplug it
        if (nativeReader != null) {
            stubPlugin.unplugStubReader(nativeReader.getName(), true);
            nativeReader.clearObservers();
        }
    }


    static public void clearStubpluginReader() throws KeypleReaderException {
        logger.info("Remove all readers from stub plugin");
        StubPlugin stubPlugin = null;
        try {
            stubPlugin =
                    (StubPlugin) SeProxyService.getInstance().getPlugin(StubPlugin.PLUGIN_NAME);
            Set<SeReader> readers = stubPlugin.getReaders();
            for (SeReader reader : readers) {
                ((ObservableReader) reader).clearObservers();
            }
            stubPlugin.unplugStubReaders(stubPlugin.getReaderNames(), true);
        } catch (KeyplePluginNotFoundException e) {
            // stub plugin is not registered
        }

    }



    protected StubReader connectStubReader(String readerName, String nodeId,
            TransmissionMode transmissionMode) throws Exception {
        // configure native reader
        StubReader nativeReader =
                (StubReader) Integration.createStubReader(readerName, transmissionMode);
        nativeReader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4,
                StubProtocolSetting.STUB_PROTOCOL_SETTING
                        .get(SeCommonProtocols.PROTOCOL_ISO14443_4));
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
