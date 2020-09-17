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
package org.eclipse.keyple.plugin.remotese.integration.test;

import java.util.UUID;
import org.eclipse.keyple.plugin.remotese.integration.common.app.ReaderEventFilter;
import org.eclipse.keyple.plugin.remotese.integration.common.endpoint.StubAsyncClientEndpoint;
import org.eclipse.keyple.plugin.remotese.integration.common.endpoint.StubAsyncServerEndpoint;
import org.eclipse.keyple.plugin.remotese.integration.common.model.UserInput;
import org.eclipse.keyple.plugin.remotese.nativese.NativeSeClientService;
import org.eclipse.keyple.plugin.remotese.nativese.impl.NativeSeClientServiceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncScenario extends BaseScenario {

  StubAsyncClientEndpoint clientEndpoint;
  private static final Logger logger = LoggerFactory.getLogger(AsyncScenario.class);
  private static StubAsyncServerEndpoint serverEndpoint;

  @BeforeClass
  static public void globalSetUp() {
    /*
     * Server side :
     * - create an isntance of the serverEndpoint
     */
    serverEndpoint = new StubAsyncServerEndpoint();
  }

  @Before
  public void setUp() {
    /*
     * Server side :
     * - retrieve remotese plugin if not initialized
     * - initialize the plugin with a async node
     * - attach the plugin observer
     */
    initRemoteSePluginWithAsyncNode(serverEndpoint);


    /*
     * <p>Client side :
     * - retrieve stub plugin if registered, retrieve stub native reader
     * - if not, register stub plugin, create a stub virtual reader
     */
    clientEndpoint = new StubAsyncClientEndpoint(serverEndpoint);
    initNativeStubPlugin();
    user1 = new UserInput().setUserId(UUID.randomUUID().toString());
  }

  @After
  public void tearDown() {
    /** Unplug the native reader */
    clearNativeReader();
  }

  /*
   * Tests
   */
  @Test
  public void execute1_localselection_remoteTransaction_successful() {

    NativeSeClientService nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withAsyncNode(clientEndpoint)
            .withoutReaderObservation()
            .getService();

    execute1_localselection_remoteTransaction_successful(nativeService);
  }

  @Test
  public void execute2_defaultSelection_onMatched_transaction_successful() {

    final ReaderEventFilter eventFilter = new ReaderEventFilter();

    NativeSeClientService nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withAsyncNode(clientEndpoint)
            .withReaderObservation(eventFilter)
            .getService();

    execute2_defaultSelection_onMatched_transaction_successful(nativeService, eventFilter);
  }

  /**
   * Similar to scenario 1 without the local aid selection. In this case, the server application is
   * responsible for ordering the aid selection.
   */
  @Test
  public void execute3_remoteselection_remoteTransaction_successful() {

    NativeSeClientService nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withAsyncNode(clientEndpoint)
            .withoutReaderObservation()
            .getService();

    execute3_remoteselection_remoteTransaction_successful(nativeService);
  }
}
