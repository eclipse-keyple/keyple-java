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
import org.eclipse.keyple.plugin.remotese.integration.common.model.DeviceInput;
import org.eclipse.keyple.plugin.remotese.integration.common.model.UserInput;
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
  public static void globalSetUp() {

    /*
     * Server side :
     * <ul>
     *   <li>create an isntance of the serverEndpoint</li>
     * </ul>
     */
    serverEndpoint = new StubAsyncServerEndpoint();
  }

  @Before
  public void setUp() {

    /*
     * Server side :
     * <ul>
     *   <li>initialize the plugin with a async server node</li>
     *   <li>attach the plugin observer</li>
     * </ul>
     */
    initRemoteSePluginWithAsyncNode(serverEndpoint);

    /*
     * Client side :
     * <ul>
     *   <li>register stub plugin</li>
     *   <li>create native stub reader</li>
     *   <li>create an async client endpoint</li>
     * <li>generate userId</li>
     * </ul>
     */
    initNativeStubPlugin();
    clientEndpoint = new StubAsyncClientEndpoint(serverEndpoint);
    user1 = new UserInput().setUserId(UUID.randomUUID().toString());
    device1 = new DeviceInput().setDeviceId(DEVICE_ID);
  }

  @After
  public void tearDown() {
    /** Unplug the native reader */
    clearNativeReader();
  }

  /** {@inheritDoc} */
  @Override
  @Test
  public void execute1_localselection_remoteTransaction_successful() {

    nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withAsyncNode(clientEndpoint)
            .withoutReaderObservation()
            .getService();

    localselection_remoteTransaction_successful();
  }

  /** {@inheritDoc} */
  @Override
  @Test
  public void execute2_defaultSelection_onMatched_transaction_successful() {

    final ReaderEventFilter eventFilter = new ReaderEventFilter();

    nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withAsyncNode(clientEndpoint)
            .withReaderObservation(eventFilter)
            .getService();

    defaultSelection_onMatched_transaction_successful(eventFilter);
  }

  /** {@inheritDoc} */
  @Override
  @Test
  public void execute3_remoteselection_remoteTransaction_successful() {

    nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withAsyncNode(clientEndpoint)
            .withoutReaderObservation()
            .getService();

    remoteselection_remoteTransaction_successful();
  }

  /** {@inheritDoc} */
  @Test
  @Override
  public void execute4_multiclient_remoteselection_remoteTransaction_successful() {

    nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withAsyncNode(clientEndpoint)
            .withoutReaderObservation()
            .getService();

    multipleclients_remoteselection_remoteTransaction_successful();
  }

  /** {@inheritDoc} */
  @Test
  @Override
  public void execute5_transaction_closeSession_fail() {
    nativeService =
            new NativeSeClientServiceFactory()
                    .builder()
                    .withAsyncNode(clientEndpoint)
                    .withoutReaderObservation()
                    .getService();

    transaction_closeSession_fail();

  }

  /** {@inheritDoc} */
  @Test
  @Override
  public void execute6_transaction_clientTimeout_fail() {
    nativeService =
            new NativeSeClientServiceFactory()
                    .builder()
                    .withAsyncNode(clientEndpoint)
                    .withoutReaderObservation()
                    .getService();

    transaction_clientTimeout_fail();
  }
}
