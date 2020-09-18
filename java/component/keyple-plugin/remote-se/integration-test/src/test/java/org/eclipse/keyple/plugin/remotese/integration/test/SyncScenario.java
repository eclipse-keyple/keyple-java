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
import org.eclipse.keyple.plugin.remotese.integration.common.endpoint.StubSyncClientEndpoint;
import org.eclipse.keyple.plugin.remotese.integration.common.model.DeviceInput;
import org.eclipse.keyple.plugin.remotese.integration.common.model.UserInput;
import org.eclipse.keyple.plugin.remotese.nativese.NativeSeClientService;
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

    clientSyncEndpoint = new StubSyncClientEndpoint();

    user1 = new UserInput().setUserId(UUID.randomUUID().toString());
    device1 = new DeviceInput().setDeviceId(DEVICE_ID);
  }

  @After
  public void tearDown() {
    /* Unplug the native reader */
    clearNativeReader();
  }

  /**
   * A successful aid selection is executed locally on the terminal followed by a remoteService call
   * to launch the remote Calypso session. The SE content is sent during this first called along
   * with custom data. All this information is received by the server to select and execute the
   * corresponding ticketing scenario.
   *
   * <p>At the end of a successful calypso session, custom data is sent back to the client as a
   * final result.
   *
   * <p>This scenario can be executed on Sync node and Async node.
   */
  @Test
  public void execute1_localselection_remoteTransaction_successful() {

    NativeSeClientService nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withSyncNode(clientSyncEndpoint)
            .withoutReaderObservation()
            .getService();

    execute1_localselection_remoteTransaction_successful(nativeService);
  }

  /**
   * The client application invokes the remoteService with enabling observability capabilities. As a
   * result the server creates a Observable Virtual Reader that receives native reader events such
   * as SE insertions and removals.
   *
   * <p>A SE Insertion is simulated locally followed by a SE removal 1 second later.
   *
   * <p>The SE Insertion event is sent to the Virtual Reader whose observer starts a remote Calypso
   * session. At the end of a successful calypso session, custom data is sent back to the client as
   * a final result.
   *
   * <p>The operation is executed twice with two different users.
   *
   * <p>After the second SE insertion, Virtual Reader observers are cleared to purge the server
   * virtual reader.
   */
  @Test
  public void execute2_defaultSelection_onMatched_transaction_successful() {

    final ReaderEventFilter eventFilter = new ReaderEventFilter();

    NativeSeClientService nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withSyncNode(clientSyncEndpoint)
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
            .withSyncNode(clientSyncEndpoint)
            .withoutReaderObservation()
            .getService();

    execute3_remoteselection_remoteTransaction_successful(nativeService);
  }

  /** Similar to scenario 3 with two concurrent clients. */
  @Test
  public void execute4_multiclient_remoteselection_remoteTransaction_successful() {

    NativeSeClientService nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withSyncNode(clientSyncEndpoint)
            .withoutReaderObservation()
            .getService();

    execute4_multipleclients_remoteselection_remoteTransaction_successful(nativeService);
  }
}
