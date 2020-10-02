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
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReader;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Test Virtual Reader Parameters with stub plugin and hoplink SE */
public class VirtualReaderParameterTest extends VirtualReaderBaseTest {

  private static final Logger logger = LoggerFactory.getLogger(VirtualReaderParameterTest.class);
  private VirtualReader virtualReader;
  private StubReader nativeReader;

  @Before
  public void setUp() throws Exception {
    Assert.assertEquals(0, SeProxyService.getInstance().getPlugins().size());

    initMasterNSlave();
  }

  @After
  public void tearDown() throws Exception {
    disconnectReader(NATIVE_READER_NAME);

    clearMasterNSlave();

    unregisterPlugins();

    Assert.assertEquals(0, SeProxyService.getInstance().getPlugins().size());
  }

  @Test
  public void testGetTransmissionMode() throws Exception {
    // configure and connect a Stub Native reader
    nativeReader = this.connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID, true);

    // test virtual reader
    virtualReader = getVirtualReader();

    Assert.assertEquals(virtualReader.isContactless(), nativeReader.isContactless());
  }

  @Test
  public void testGetSession() throws Exception {
    // configure and connect a Stub Native reader
    nativeReader = this.connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID, true);

    // test virtual reader
    virtualReader = getVirtualReader();

    Assert.assertNotNull(virtualReader.getSession().getCreatedTime());
    Assert.assertNotNull(virtualReader.getSession().getSessionId());
    Assert.assertNotNull(virtualReader.getSession().getMasterNodeId(), SERVER_NODE_ID);
    Assert.assertNotNull(virtualReader.getSession().getSlaveNodeId(), CLIENT_NODE_ID);
  }
}
