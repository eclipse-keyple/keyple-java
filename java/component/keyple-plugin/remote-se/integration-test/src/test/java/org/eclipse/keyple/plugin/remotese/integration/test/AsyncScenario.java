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
import org.eclipse.keyple.plugin.remotese.core.exception.KeypleTimeoutException;
import org.eclipse.keyple.plugin.remotese.core.impl.AbstractKeypleNode;
import org.eclipse.keyple.plugin.remotese.integration.common.app.ReaderEventFilter;
import org.eclipse.keyple.plugin.remotese.integration.common.endpoint.StubAsyncClientEndpoint;
import org.eclipse.keyple.plugin.remotese.integration.common.endpoint.StubAsyncServerEndpoint;
import org.eclipse.keyple.plugin.remotese.integration.common.endpoint.StubNetworkConnectionException;
import org.eclipse.keyple.plugin.remotese.integration.common.model.DeviceInput;
import org.eclipse.keyple.plugin.remotese.integration.common.model.UserInput;
import org.eclipse.keyple.plugin.remotese.nativese.impl.NativeSeClientServiceFactory;
import org.eclipse.keyple.plugin.remotese.nativese.impl.NativeSeClientUtils;
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
    clientEndpoint = new StubAsyncClientEndpoint(serverEndpoint, false);
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
  public void execute_localselection_remoteTransaction_successful() {

    nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withAsyncNode(clientEndpoint)
            .useDefaultTimeout()
            .withoutReaderObservation()
            .getService();

    localselection_remoteTransaction_successful();
  }

  /** {@inheritDoc} */
  @Override
  @Test
  public void execute_remoteselection_remoteTransaction_successful() {

    nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withAsyncNode(clientEndpoint)
            .useDefaultTimeout()
            .withoutReaderObservation()
            .getService();

    remoteselection_remoteTransaction_successful();
  }

  /** {@inheritDoc} */
  @Test
  @Override
  public void execute_multiclient_remoteselection_remoteTransaction_successful() {

    nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withAsyncNode(clientEndpoint)
            .useDefaultTimeout()
            .withoutReaderObservation()
            .getService();

    multipleclients_remoteselection_remoteTransaction_successful();
  }

  /** {@inheritDoc} */
  @Test
  @Override
  public void execute_transaction_closeSession_SE_error() {
    nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withAsyncNode(clientEndpoint)
            .useDefaultTimeout()
            .withoutReaderObservation()
            .getService();

    transaction_closeSession_fail();
  }

  /** {@inheritDoc} */
  @Test(expected = StubNetworkConnectionException.class)
  @Override
  public void execute_transaction_host_network_error() {
    clientEndpoint = new StubAsyncClientEndpoint(serverEndpoint, true);

    nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withAsyncNode(clientEndpoint)
            .useDefaultTimeout()
            .withoutReaderObservation()
            .getService();
    remoteselection_remoteTransaction();
    // throw exception
  }

  @Test(expected = KeypleTimeoutException.class)
  @Override
  public void execute_transaction_client_network_error() {
    serverEndpoint.setSimulateConnectionError(true);
    nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withAsyncNode(clientEndpoint)
            .useCustomTimeout(2)
            .withoutReaderObservation()
            .getService();

    setTimeoutInNode((AbstractKeypleNode) NativeSeClientUtils.getAsyncNode(), 2000);

    remoteselection_remoteTransaction();
  }

  /** {@inheritDoc} */
  @Test
  @Override
  public void execute_transaction_slowSe_success() {
    nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withAsyncNode(clientEndpoint)
            .useDefaultTimeout()
            .withoutReaderObservation()
            .getService();

    transaction_slowSe_success();
  }

  @Override
  @Test
  public void execute_all_methods() {
    final ReaderEventFilter eventFilter = new ReaderEventFilter();

    nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withAsyncNode(clientEndpoint)
            .useDefaultTimeout()
            .withReaderObservation(eventFilter)
            .getService();

    all_methods();
  }

  /** {@inheritDoc} */
  @Override
  @Test
  public void observable_defaultSelection_onMatched_transaction_successful() {

    final ReaderEventFilter eventFilter = new ReaderEventFilter();

    nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withAsyncNode(clientEndpoint)
            .useDefaultTimeout()
            .withReaderObservation(eventFilter)
            .getService();

    defaultSelection_onMatched_transaction_successful(eventFilter);
  }
}
