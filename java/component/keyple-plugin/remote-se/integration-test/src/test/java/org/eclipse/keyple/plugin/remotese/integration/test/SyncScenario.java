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

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientSync;
import org.eclipse.keyple.plugin.remotese.integration.common.endpoint.StubSyncClientEndpoint;
import org.eclipse.keyple.plugin.remotese.integration.common.model.ConfigurationResult;
import org.eclipse.keyple.plugin.remotese.integration.common.model.DeviceInput;
import org.eclipse.keyple.plugin.remotese.integration.common.model.TransactionResult;
import org.eclipse.keyple.plugin.remotese.integration.common.model.UserInput;
import org.eclipse.keyple.plugin.remotese.integration.common.se.StubCalypsoClassic;
import org.eclipse.keyple.plugin.remotese.nativese.NativeSeClientService;
import org.eclipse.keyple.plugin.remotese.nativese.RemoteServiceParameters;
import org.eclipse.keyple.plugin.remotese.nativese.impl.NativeSeClientServiceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncScenario extends BaseScenario {

  KeypleClientSync clientSyncEndpoint;
  private static final Logger logger = LoggerFactory.getLogger(SyncScenario.class);

  @Before
  public void setUp() {

    /*
     * Server side :
     * - retrieve remotese plugin if not initialized
     * - initialize the plugin with a sync node
     * attach the plugin observer
     */
    initRemoteSePluginWithSyncNode();

    /*
     * <p>Client side : - retrieve stub plugin if registered, retrieve stub native reader - if not,
     * register stub plugin, create a stub virtual reader
     */
    initNativeStubPlugin();

    clientSyncEndpoint = new StubSyncClientEndpoint();

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

    // insert stub SE into stub
    nativeReader.insertSe(new StubCalypsoClassic());

    CalypsoPo calypsoPo = explicitPoSelection();

    // execute remote service
    TransactionResult output =
        nativeService.executeRemoteService(
            RemoteServiceParameters.builder(SERVICE_ID_1, nativeReader)
                .withInitialSeContext(calypsoPo)
                .withUserInputData(new UserInput().setUserId(user1.getUserId()))
                .build(),
            TransactionResult.class);

    // validate result
    assertThat(output.isSuccessful()).isTrue();
    assertThat(output.getUserId()).isEqualTo(user1.getUserId());
  }

  @Test
  public void execute2_defaultSelection_onMatched_transaction_successful()
      throws InterruptedException {

    final ReaderEventFilter eventFilter = new ReaderEventFilter();

    NativeSeClientService nativeService =
        new NativeSeClientServiceFactory()
            .builder()
            .withSyncNode(clientSyncEndpoint)
            .withReaderObservation(eventFilter)
            .getService();

    // execute remote service to create observable virtual reader
    ConfigurationResult configurationResult =
        nativeService.executeRemoteService(
            RemoteServiceParameters.builder(SERVICE_ID_2, nativeReader)
                .withUserInputData(new DeviceInput().setDeviceId(DEVICE_ID))
                .build(),
            ConfigurationResult.class);

    assertThat(configurationResult.isSuccessful()).isTrue();
    assertThat(configurationResult.getDeviceId()).isEqualTo(DEVICE_ID);

    eventFilter.setUser(user1);

    // user1 insert SE , SE event should be sent to server
    nativeReader.insertSe(new StubCalypsoClassic());
    logger.info(
        "1 - Verify User Transaction is successful for first user {}",
        eventFilter.user.getUserId());
    // filter
    await().atMost(2, TimeUnit.SECONDS).until(verifyUserTransaction(eventFilter, user1));

    nativeReader.removeSe();

    await().atMost(1, TimeUnit.SECONDS).until(seRemoved(nativeReader));

    UserInput user2 = new UserInput().setUserId(UUID.randomUUID().toString());
    eventFilter.setUser(user2);
    eventFilter.transactionResult = null;

    // user2 insert SE , SE event should be sent to server
    nativeReader.insertSe(new StubCalypsoClassic());
    logger.info(
        "2 - Verify User Transaction is successful for second user {}",
        eventFilter.user.getUserId());
    // filter
    await().atMost(2, TimeUnit.SECONDS).until(verifyUserTransaction(eventFilter, user2));

    nativeReader.removeSe();
    await().atMost(1, TimeUnit.SECONDS).until(seRemoved(nativeReader));
  }
}
