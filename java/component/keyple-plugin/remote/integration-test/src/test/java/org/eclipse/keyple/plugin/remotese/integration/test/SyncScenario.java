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
import org.eclipse.keyple.plugin.remotese.core.KeypleClientSync;
import org.eclipse.keyple.plugin.remotese.integration.common.app.ReaderEventFilter;
import org.eclipse.keyple.plugin.remotese.integration.common.endpoint.StubNetworkConnectionException;
import org.eclipse.keyple.plugin.remotese.integration.common.endpoint.StubSyncClientEndpoint;
import org.eclipse.keyple.plugin.remotese.integration.common.model.DeviceInput;
import org.eclipse.keyple.plugin.remotese.integration.common.model.UserInput;
import org.eclipse.keyple.plugin.remotese.nativese.impl.NativeSeClientServiceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncScenario extends BaseScenario {

  private static final Logger logger = LoggerFactory.getLogger(SyncScenario.class);

  KeypleClientSync clientSyncEndpoint;

  @Before
  public void setUp() {

    /*
     * Server side :
     * <ul>
     *   <li>initialize the plugin with a sync node</li>
     *   <li>attach the plugin observer</li>
     * </ul>
     */
    initRemoteSePluginWithSyncNode();

    /*
     * Client side :
     * <ul>
     *   <li>register stub plugin</li>
     *   <li>create native stub reader</li>
     *   <li>create a sync client endpoint</li>
     *  <li>generate userId</li>
     * </ul>
     */
    initNativeStubPlugin();

    clientSyncEndpoint = new StubSyncClientEndpoint(false);

    user1 = new UserInput().setUserId(UUID.randomUUID().toString());
    device1 = new DeviceInput().setDeviceId(DEVICE_ID);
  }

  @After
  public void tearDown() {
    /* Unplug the native reader */
    clearNativeReader();
  }

  /** {@inheritDoc} */
  @Override
  @Test
  public void execute_localselection_remoteTransaction_successful() {

    nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withSyncNode(clientSyncEndpoint)
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
            .withSyncNode(clientSyncEndpoint)
            .withoutReaderObservation()
            .getService();

    remoteselection_remoteTransaction_successful();
  }

  /** {@inheritDoc} */
  @Override
  @Test
  public void execute_multiclient_remoteselection_remoteTransaction_successful() {

    nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withSyncNode(clientSyncEndpoint)
            .withoutReaderObservation()
            .getService();

    multipleclients_remoteselection_remoteTransaction_successful();
  }

  /** {@inheritDoc} */
  @Override
  @Test
  public void execute_transaction_closeSession_SE_error() {
    nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withSyncNode(clientSyncEndpoint)
            .withoutReaderObservation()
            .getService();

    transaction_closeSession_fail();
  }

  /** {@inheritDoc} */
  @Override
  @Test(expected = StubNetworkConnectionException.class)
  public void execute_transaction_host_network_error() {
    clientSyncEndpoint = new StubSyncClientEndpoint(true);

    nativeService =
        new NativeSeClientServiceFactory()
            .builder()
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

  /** {@inheritDoc} */
  @Override
  @Test
  public void execute_transaction_slowSe_success() {
    nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withSyncNode(clientSyncEndpoint)
            .withoutReaderObservation()
            .getService();

    transaction_slowSe_success();
  }

  @Test
  @Override
  public void execute_all_methods() {
    final ReaderEventFilter eventFilter = new ReaderEventFilter();

    nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withSyncNode(clientSyncEndpoint)
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
            .withSyncNode(clientSyncEndpoint)
            .withReaderObservation(eventFilter)
            .getService();

    defaultSelection_onMatched_transaction_successful(eventFilter);
  }
}
