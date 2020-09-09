/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.remotese.virtualse.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerVirtualObserverReaderTest extends RemoteSeServerBaseTest {

  @Before
  public void setUp() {
    pluginObserver = new MockPluginObserver(true);
    readerObserver = new MockReaderObserver();
    registerSyncPlugin();
    assertThat(remoteSePlugin).isNotNull();
  }

  @After
  public void tearDown() {
    unregisterPlugin();
  }

  @Test
  public void
      onMessage_executeRemoteService_createObservableVirtualReader_deleteObserver_shouldDeleteReader() {
    String sessionId = UUID.randomUUID().toString();
    KeypleMessageDto message = executeRemoteServiceMessage(sessionId, true);
    remoteSePlugin.onMessage(message);
    ServerVirtualObservableReader virtualReader =
        (ServerVirtualObservableReader) remoteSePlugin.getReaders().values().iterator().next();

    virtualReader.removeObserver(readerObserver);
    assertThat(remoteSePlugin.getReaders()).hasSize(0);
  }
}
