/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.remotese.integration;

import org.eclipse.keyple.core.seproxy.Reader;
import org.eclipse.keyple.core.seproxy.SmartCardService;
import org.eclipse.keyple.plugin.remotese.nativese.SlaveAPI;
import org.eclipse.keyple.plugin.remotese.pluginse.MasterAPI;
import org.eclipse.keyple.plugin.remotese.pluginse.RemoteSePoolPlugin;
import org.eclipse.keyple.plugin.remotese.transport.impl.java.LocalTransportFactory;
import org.eclipse.keyple.plugin.stub.StubPoolPlugin;
import org.eclipse.keyple.plugin.stub.StubSecureElement;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Test RemoteSePoolPluginTest */
public class RemoteSePoolPluginTest {

  private static final Logger logger = LoggerFactory.getLogger(RemoteSePoolPluginTest.class);

  // input
  private LocalTransportFactory factory;
  private MasterAPI masterAPI;
  private StubPoolPlugin stubPoolPlugin;
  private SlaveAPI slaveAPI;

  private SmartCardService smartCardService = SmartCardService.getInstance();

  // created by masterAPI
  private RemoteSePoolPlugin remoteSePoolPlugin;

  final String CLIENT_NODE_ID = "testClientNodeId";
  final String SERVER_NODE_ID = "testServerNodeId";

  String REF_GROUP1 = "REF_GROUP1";

  /** Slave is Server with StubPoolPlugin Master is Client with RemoteSePoolPlugin */
  @Before
  public void setUp() throws Exception {

    Assert.assertEquals(0, smartCardService.getPlugins().size());

    // create local transportfactory
    factory = new LocalTransportFactory(SERVER_NODE_ID);

    /*
     * Configure slave with Stub Pool Plugin
     */

    // create stub pool plugin
    stubPoolPlugin = Integration.createStubPoolPlugin();

    // plug readers
    stubPoolPlugin.plugStubPoolReader(REF_GROUP1, "stub1", stubSe);

    // configure Slave with Stub Pool plugin and local server node
    slaveAPI = new SlaveAPI(SmartCardService.getInstance(), factory.getServer(), "");

    // bind slaveAPI to stubPoolPlugin
    slaveAPI.registerReaderPoolPlugin(stubPoolPlugin);

    /*
     * Configure master with Remote Pool Plugin
     */

    // configure Master with RemoteSe Pool plugin and client node
    masterAPI =
        new MasterAPI(
            SmartCardService.getInstance(),
            factory.getClient(CLIENT_NODE_ID),
            10000,
            MasterAPI.PLUGIN_TYPE_POOL,
            "REMOTESE_POOL_PLUGIN1");

    remoteSePoolPlugin = (RemoteSePoolPlugin) masterAPI.getPlugin();
  }

  @After
  public void tearDown() throws Exception {
    SmartCardService.getInstance().unregisterPlugin(stubPoolPlugin.getName());
    SmartCardService.getInstance().unregisterPlugin(remoteSePoolPlugin.getName());
    remoteSePoolPlugin = null;
    stubPoolPlugin = null;
    masterAPI = null;
    factory = null;
    slaveAPI = null;

    Assert.assertEquals(0, smartCardService.getPlugins().size());
  }

  /** Test allocate SUCCESS */
  @Test
  public void allocate_success() throws Exception {

    // allocate reader
    remoteSePoolPlugin.bind(SERVER_NODE_ID);
    Reader reader = remoteSePoolPlugin.allocateReader(REF_GROUP1);

    // check results
    Assert.assertNotNull(reader);
  }

  /** Test release SUCCESS */
  @Test
  public void release_success() throws Exception {

    // allocate reader
    remoteSePoolPlugin.bind(SERVER_NODE_ID);
    Reader reader = remoteSePoolPlugin.allocateReader(REF_GROUP1);

    // release reader
    ((RemoteSePoolPlugin) masterAPI.getPlugin()).releaseReader(reader);

    // re allocate reader
    Reader reader2 = remoteSePoolPlugin.allocateReader(REF_GROUP1);

    // check results
    Assert.assertNotNull(reader2);
  }

  /** Stub card */
  private static final StubSecureElement stubSe =
      new StubSecureElement() {
        @Override
        public byte[] getATR() {
          return new byte[0];
        }

        @Override
        public String getSeProtocol() {
          return "ISO_14443_4";
        }
      };
}
