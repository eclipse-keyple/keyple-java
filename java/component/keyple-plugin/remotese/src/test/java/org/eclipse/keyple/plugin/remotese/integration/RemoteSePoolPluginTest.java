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



import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.plugin.remotese.nativese.SlaveAPI;
import org.eclipse.keyple.plugin.remotese.pluginse.MasterAPI;
import org.eclipse.keyple.plugin.remotese.pluginse.RemoteSePoolPlugin;
import org.eclipse.keyple.plugin.remotese.transport.impl.java.LocalTransportFactory;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubPoolPlugin;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test RemoteSePoolPluginTest
 */
public class RemoteSePoolPluginTest {

    private static final Logger logger = LoggerFactory.getLogger(RemoteSePoolPluginTest.class);

    // input
    private LocalTransportFactory factory;
    private MasterAPI masterAPI;
    private StubPoolPlugin stubPoolPlugin;
    private SlaveAPI slaveAPI;

    // created by masterAPI
    private RemoteSePoolPlugin remoteSePoolPlugin;

    final String CLIENT_NODE_ID = "testClientNodeId";
    final String CLIENT_NODE_ID_2 = "testClientNodeId2";
    final String SERVER_NODE_ID = "testServerNodeId";

    /**
     * Slave is Server with StubPoolPlugin Master is Client with RemoteSePoolPlugin
     */
    @Before
    public void setUp() throws Exception {
        assert StubPlugin.getInstance().getReaders().size() == 0;

        // create local transportfactory
        factory = new LocalTransportFactory(SERVER_NODE_ID);

        // create stub pool plugin
        stubPoolPlugin = Integration.createStubPoolPlugin();

        // configure Slave with Stub Pool plugin and local server node
        slaveAPI = new SlaveAPI(SeProxyService.getInstance(), factory.getServer(), "");
        slaveAPI.registerReaderPoolPlugin(stubPoolPlugin);

        // configure Master with RemoteSe Pool plugin and client node
        masterAPI = new MasterAPI(SeProxyService.getInstance(), factory.getClient(CLIENT_NODE_ID),
                10000, MasterAPI.PLUGIN_TYPE_POOL);

        remoteSePoolPlugin = (RemoteSePoolPlugin) masterAPI.getPlugin();


    }

    @After
    public void tearDown() throws Exception {
        VirtualReaderBaseTest.clearStubpluginReader();
    }

    /**
     * Test allocate SUCCESS
     */
    @Test
    public void testAllocate_success() throws Exception {
        String REF_GROUP1 = "REF_GROUP1";

        // allocate reader
        remoteSePoolPlugin.bind(SERVER_NODE_ID);
        SeReader seReader = remoteSePoolPlugin.allocateReader(REF_GROUP1);

        // check results
        Assert.assertTrue(seReader.getName().contains(REF_GROUP1));
        Assert.assertEquals(1, masterAPI.getPlugin().getReaders().size());

    }

    /**
     * Test allocate FAIL
     */
    @Test
    public void testAllocate_fail() throws Exception {}

    /**
     * Test release SUCCESS
     */
    @Test
    public void testRelease_success() throws Exception {
        String REF_GROUP1 = "REF_GROUP1";

        // allocate reader
        remoteSePoolPlugin.bind(SERVER_NODE_ID);
        SeReader seReader = remoteSePoolPlugin.allocateReader(REF_GROUP1);

        // release reader
        ((RemoteSePoolPlugin) masterAPI.getPlugin()).releaseReader(seReader);

        // check results
        Assert.assertEquals(0, masterAPI.getPlugin().getReaders().size());

    }

    /**
     * Test release FAIL
     */
    @Test
    public void testRelease_fail() throws Exception {}

}
