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

import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientSync;
import org.eclipse.keyple.plugin.remotese.integration.common.endpoint.StubSyncClient;
import org.eclipse.keyple.plugin.remotese.integration.common.model.UserInput;
import org.eclipse.keyple.plugin.remotese.integration.common.model.UserOutput;
import org.eclipse.keyple.plugin.remotese.integration.common.se.StubCalypsoClassic;
import org.eclipse.keyple.plugin.remotese.nativese.NativeSeClientService;
import org.eclipse.keyple.plugin.remotese.nativese.RemoteServiceParameters;
import org.eclipse.keyple.plugin.remotese.nativese.impl.NativeSeClientServiceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SyncScenario extends BaseScenario {

  /**
   * Server side : - retrieve remotese plugin if not initialized - initialize the plugin with a sync
   * node, attach the plugin observer
   *
   * <p>Client side : - retrieve stub plugin if registered, retrieve stub native reader - if not,
   * register stub plugin, create a stub virtual reader
   */
  KeypleClientSync clientSyncEndpoint;

  @Before
  public void setUp() {

    initRemoteSePluginWithSyncNode();

    // client side
    initNativeStubPlugin();

    clientSyncEndpoint = new StubSyncClient();
  }

  @After
  public void tearDown() {}

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
  public void execute1_localselection_remotePoTransaction() {

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
    UserOutput output =
            nativeService.executeRemoteService(
                    RemoteServiceParameters.builder(SERVICE_ID_1, nativeReader)
                            .withInitialSeContext(calypsoPo)
                            .withUserInputData(new UserInput().setUserId(USER_ID))
                            .build(),
                    UserOutput.class);

    // validate result
    assertThat(output.getSuccessful()).isTrue();
    assertThat(output.getUserId()).isEqualTo(USER_ID);
  }

  @Test
  public void execute2() {

  }
}
