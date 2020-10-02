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

import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.util.SeCommonProtocols;
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

/** Test Virtual Reader Service with stub plugin and hoplink SE */
public class VirtualReaderBaseTest {

  @Rule public TestName name = new TestName();

  private static final Logger logger = LoggerFactory.getLogger(VirtualReaderBaseTest.class);

  // Real objects
  protected TransportFactory factory;

  protected final String NATIVE_READER_NAME = "testStubReader";
  protected final String CLIENT_NODE_ID = "testClientNodeId";
  protected final String SERVER_NODE_ID = "testServerNodeId";

  protected SeProxyService seProxyService = SeProxyService.getInstance();

  protected final String REMOTE_SE_PLUGIN_NAME = "remoteseplugin1";

  // Spy Object
  protected MasterAPI masterAPI;
  // Spy Object
  protected SlaveAPI slaveAPI;

  protected void initMasterNSlave() throws Exception {
    logger.info("------------------------------");
    logger.info("Test {}", name.getMethodName());
    logger.info("------------------------------");

    logger.info("*** Init LocalTransportFactory");
    // use a local transport factory for testing purposes (only java calls between client and
    // server). Only one client and one server bound together.
    factory = new LocalTransportFactory(SERVER_NODE_ID);

    logger.info("*** Bind Master Services");
    // bind Master services to server
    masterAPI = Integration.createSpyMasterAPI(factory.getServer(), REMOTE_SE_PLUGIN_NAME);

    logger.info("*** Bind Slave Services");
    // bind Slave services to client
    slaveAPI = Integration.createSpySlaveAPI(factory.getClient(CLIENT_NODE_ID), SERVER_NODE_ID);
  }

  protected void clearMasterNSlave() {
    factory = null;
    masterAPI = null;
    slaveAPI = null;
  }

  protected void unregisterPlugins() {
    seProxyService.unregisterPlugin(Integration.SLAVE_POOL_STUB);
    seProxyService.unregisterPlugin(Integration.SLAVE_STUB);
    seProxyService.unregisterPlugin(REMOTE_SE_PLUGIN_NAME);
  }

  public void disconnectReader(String readerName) {
    logger.info("Remove all readers from stub plugin");
    StubPlugin stubPlugin = null;
    try {
      stubPlugin = (StubPlugin) SeProxyService.getInstance().getPlugin(Integration.SLAVE_STUB);

      // Set<SeReader> readers = stubPlugin.getReaders();

      /*
       * unplug each readers and check that there are no observers
       */
      /*
       * for (SeReader reader : readers) { Assert.assertEquals(0, ((ObservableReader)
       * reader).countObservers()); ((ObservableReader) reader).clearObservers(); }
       */
      this.slaveAPI.disconnectReader("", readerName);
      stubPlugin.unplugStubReader(readerName, true);

    } catch (KeyplePluginNotFoundException e) {
      // stub plugin is not registered
    }
  }

  protected StubReader connectStubReader(String readerName, String nodeId, boolean isContactless)
      throws Exception {
    // configure native reader
    StubReader nativeReader = (StubReader) Integration.createStubReader(readerName, isContactless);

    // activate PROTOCOL_ISO14443_4
    nativeReader.activateProtocol(SeCommonProtocols.PROTOCOL_ISO14443_4.getDescriptor());

    this.slaveAPI.connectReader(nativeReader);
    return nativeReader;
  }

  protected void disconnectStubReader(String sessionId, String nativeReaderName, String nodeId)
      throws Exception {
    this.slaveAPI.disconnectReader(sessionId, nativeReaderName);
  }

  protected VirtualReader getVirtualReader() throws Exception {
    Assert.assertEquals(1, this.masterAPI.getPlugin().getReaders().size());
    return (VirtualReader) this.masterAPI.getPlugin().getReaders().values().toArray()[0];
  }
}
