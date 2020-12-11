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
package org.eclipse.keyple.plugin.remote.integration.service;

import java.util.UUID;
import org.eclipse.keyple.plugin.remote.NodeCommunicationException;
import org.eclipse.keyple.plugin.remote.impl.LocalServiceClientFactory;
import org.eclipse.keyple.plugin.remote.integration.common.app.ReaderEventFilter;
import org.eclipse.keyple.plugin.remote.integration.common.endpoint.StubNetworkConnectionException;
import org.eclipse.keyple.plugin.remote.integration.common.endpoint.service.StubAsyncEndpointClient;
import org.eclipse.keyple.plugin.remote.integration.common.endpoint.service.StubAsyncEndpointServer;
import org.eclipse.keyple.plugin.remote.integration.common.model.DeviceInput;
import org.eclipse.keyple.plugin.remote.integration.common.model.UserInput;
import org.junit.*;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncScenario extends BaseScenario {

  StubAsyncEndpointClient clientEndpoint;
  private static final Logger logger = LoggerFactory.getLogger(AsyncScenario.class);
  private static StubAsyncEndpointServer serverEndpoint;

  @Rule public TestName testName = new TestName();

  @BeforeClass
  public static void globalSetUp() {

    /*
     * Server side :
     * <ul>
     *   <li>create an isntance of the serverEndpoint</li>
     * </ul>
     */
    serverEndpoint = new StubAsyncEndpointServer();
  }

  @Before
  public void setUp() {

    localServiceName = testName + "_async";

    /*
     * Server side :
     * <ul>
     *   <li>initialize the plugin with a async server node</li>
     *   <li>attach the plugin observer</li>
     * </ul>
     */
    initRemotePluginWithAsyncNode(serverEndpoint);

    /*
     * Client side :
     * <ul>
     *   <li>register stub plugin</li>
     *   <li>create local stub reader</li>
     *   <li>create an async client endpoint</li>
     * <li>generate userId</li>
     * </ul>
     */
    initNativeStubPlugin();
    clientEndpoint = new StubAsyncEndpointClient(serverEndpoint, false, localServiceName);
    user1 = new UserInput().setUserId(UUID.randomUUID().toString());
    device1 = new DeviceInput().setDeviceId(DEVICE_ID);
  }

  @After
  public void tearDown() {
    /** Unplug the local reader */
    clearNativeReader();
  }

  @AfterClass
  public static void globalTearDown() {
    unRegisterRemotePlugin();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  @Test
  public void execute_localselection_remoteTransaction_successful() {

    localService =
        LocalServiceClientFactory.builder()
            .withServiceName(localServiceName)
            .withAsyncNode(clientEndpoint)
            .usingDefaultTimeout()
            .withoutReaderObservation()
            .getService();

    localselection_remoteTransaction_successful();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  @Test
  public void execute_remoteselection_remoteTransaction_successful() {

    localService =
        LocalServiceClientFactory.builder()
            .withServiceName(localServiceName)
            .withAsyncNode(clientEndpoint)
            .usingDefaultTimeout()
            .withoutReaderObservation()
            .getService();

    remoteselection_remoteTransaction_successful();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Test
  @Override
  public void execute_multiclient_remoteselection_remoteTransaction_successful() {

    localService =
        LocalServiceClientFactory.builder()
            .withServiceName(localServiceName)
            .withAsyncNode(clientEndpoint)
            .usingDefaultTimeout()
            .withoutReaderObservation()
            .getService();

    multipleclients_remoteselection_remoteTransaction_successful();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Test
  @Override
  public void execute_transaction_closeSession_card_error() {
    localService =
        LocalServiceClientFactory.builder()
            .withServiceName(localServiceName)
            .withAsyncNode(clientEndpoint)
            .usingDefaultTimeout()
            .withoutReaderObservation()
            .getService();

    transaction_closeSession_fail();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Test(expected = StubNetworkConnectionException.class)
  @Override
  public void execute_transaction_host_network_error() {
    clientEndpoint = new StubAsyncEndpointClient(serverEndpoint, true, localServiceName);

    localService =
        LocalServiceClientFactory.builder()
            .withServiceName(localServiceName)
            .withAsyncNode(clientEndpoint)
            .usingDefaultTimeout()
            .withoutReaderObservation()
            .getService();
    remoteselection_remoteTransaction();
    // throw exception
  }

  @Test(expected = NodeCommunicationException.class)
  @Override
  public void execute_transaction_client_network_error() {
    serverEndpoint.setSimulateConnectionError(true);
    localService =
        LocalServiceClientFactory.builder()
            .withServiceName(localServiceName)
            .withAsyncNode(clientEndpoint)
            .usingTimeout(2)
            .withoutReaderObservation()
            .getService();

    remoteselection_remoteTransaction();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Test
  @Override
  public void execute_transaction_slowSe_success() {
    localService =
        LocalServiceClientFactory.builder()
            .withServiceName(localServiceName)
            .withAsyncNode(clientEndpoint)
            .usingDefaultTimeout()
            .withoutReaderObservation()
            .getService();

    transaction_slowSe_success();
  }

  @Override
  @Test
  public void execute_all_methods() {
    final ReaderEventFilter eventFilter = new ReaderEventFilter();

    localService =
        LocalServiceClientFactory.builder()
            .withServiceName(localServiceName)
            .withAsyncNode(clientEndpoint)
            .usingDefaultTimeout()
            .withReaderObservation(eventFilter)
            .getService();

    all_methods();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  @Test
  public void observable_defaultSelection_onMatched_transaction_successful() {

    final ReaderEventFilter eventFilter = new ReaderEventFilter();

    localService =
        LocalServiceClientFactory.builder()
            .withServiceName(localServiceName)
            .withAsyncNode(clientEndpoint)
            .usingDefaultTimeout()
            .withReaderObservation(eventFilter)
            .getService();

    defaultSelection_onMatched_transaction_successful(eventFilter);
  }
}
