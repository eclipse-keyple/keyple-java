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
package org.eclipse.keyple.distributed.integration.service;

import java.util.UUID;
import org.eclipse.keyple.distributed.impl.LocalServiceClientFactory;
import org.eclipse.keyple.distributed.integration.common.app.ReaderEventFilter;
import org.eclipse.keyple.distributed.integration.common.endpoint.StubNetworkConnectionException;
import org.eclipse.keyple.distributed.integration.common.endpoint.service.StubSyncEndpointClient;
import org.eclipse.keyple.distributed.integration.common.model.DeviceInput;
import org.eclipse.keyple.distributed.integration.common.model.UserInput;
import org.eclipse.keyple.distributed.spi.SyncEndpointClient;
import org.junit.*;
import org.junit.rules.TestName;

public class SyncScenario extends BaseScenario {

  SyncEndpointClient clientSyncEndpoint;

  @Rule public TestName testName = new TestName();

  @Before
  public void setUp() {

    localServiceName = testName.getMethodName() + "_sync";
    /*
     * Server side :
     * <ul>
     *   <li>initialize the plugin with a sync node</li>
     *   <li>attach the plugin observer</li>
     * </ul>
     */
    initRemotePluginWithSyncNode();

    /*
     * Client side :
     * <ul>
     *   <li>register stub plugin</li>
     *   <li>create local stub reader</li>
     *   <li>create a sync client endpoint</li>
     *  <li>generate userId</li>
     * </ul>
     */
    initNativeStubPlugin();

    clientSyncEndpoint = new StubSyncEndpointClient(false);

    user1 = new UserInput().setUserId(UUID.randomUUID().toString());
    device1 = new DeviceInput().setDeviceId(DEVICE_ID);
  }

  @After
  public void tearDown() {
    /* Unplug the local reader */
    clearNativeReader();
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
            .withSyncNode(clientSyncEndpoint)
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
            .withSyncNode(clientSyncEndpoint)
            .withoutReaderObservation()
            .getService();

    remoteselection_remoteTransaction_successful();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  @Test
  public void execute_multiclient_remoteselection_remoteTransaction_successful() {

    localService =
        LocalServiceClientFactory.builder()
            .withServiceName(localServiceName)
            .withSyncNode(clientSyncEndpoint)
            .withoutReaderObservation()
            .getService();

    multipleclients_remoteselection_remoteTransaction_successful();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  @Test
  public void execute_transaction_closeSession_card_error() {
    localService =
        LocalServiceClientFactory.builder()
            .withServiceName(localServiceName)
            .withSyncNode(clientSyncEndpoint)
            .withoutReaderObservation()
            .getService();

    transaction_closeSession_fail();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  @Test(expected = StubNetworkConnectionException.class)
  public void execute_transaction_host_network_error() {
    clientSyncEndpoint = new StubSyncEndpointClient(true);

    localService =
        LocalServiceClientFactory.builder()
            .withServiceName(localServiceName)
            .withSyncNode(clientSyncEndpoint)
            .withoutReaderObservation()
            .getService();

    remoteselection_remoteTransaction();
    // throw exception
  }

  @Test
  @Override
  public void execute_transaction_client_network_error() {
    // not needed for sync node
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  @Test
  public void execute_transaction_slowSe_success() {
    localService =
        LocalServiceClientFactory.builder()
            .withServiceName(localServiceName)
            .withSyncNode(clientSyncEndpoint)
            .withoutReaderObservation()
            .getService();

    transaction_slowSe_success();
  }

  @Test
  @Override
  public void execute_all_methods() {
    final ReaderEventFilter eventFilter = new ReaderEventFilter();

    localService =
        LocalServiceClientFactory.builder()
            .withServiceName(localServiceName)
            .withSyncNode(clientSyncEndpoint)
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
            .withSyncNode(clientSyncEndpoint)
            .withReaderObservation(eventFilter)
            .getService();

    defaultSelection_onMatched_transaction_successful(eventFilter);
  }
}
