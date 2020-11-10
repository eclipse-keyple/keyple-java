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
package org.eclipse.keyple.plugin.remote.integration.pool;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.SortedSet;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.plugin.remote.spi.SyncEndpointClient;
import org.eclipse.keyple.plugin.remote.integration.common.endpoint.pool.StubSyncClientEndpoint;
import org.eclipse.keyple.plugin.remote.integration.common.util.CalypsoUtilities;
import org.eclipse.keyple.plugin.remote.impl.NativePoolServerServiceFactory;
import org.eclipse.keyple.plugin.remote.RemotePoolClientPlugin;
import org.eclipse.keyple.plugin.remote.impl.RemotePoolClientPluginFactory;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncScenario extends BaseScenario {

  private static final Logger logger = LoggerFactory.getLogger(SyncScenario.class);

  @Before
  public void setUp() {
    initNativePoolStubPlugin();

    SyncEndpointClient clientEndpoint = new StubSyncClientEndpoint();

    nativePoolServerService =
        new NativePoolServerServiceFactory()
            .builder()
            .withSyncNode()
            .withPoolPlugins(nativePoolPlugin.getName())
            .getService();

    remotePoolClientPlugin =
        (RemotePoolClientPlugin)
            SmartCardService.getInstance()
                .registerPlugin(
                    RemotePoolClientPluginFactory.builder()
                        .withSyncNode(clientEndpoint)
                        .usingDefaultTimeout()
                        .build());
  }

  @Test
  @Override
  public void execute_transaction_on_pool_reader() {
    SortedSet<String> groupReferences = remotePoolClientPlugin.getReaderGroupReferences();
    assertThat(groupReferences).containsExactly(groupReference);

    Reader virtualReader = remotePoolClientPlugin.allocateReader(groupReference);
    CardSelection seSelection = CalypsoUtilities.getSeSelection();
    CalypsoPo calypsoPo =
        (CalypsoPo) seSelection.processExplicitSelection(virtualReader).getActiveSmartCard();

    String eventLog = CalypsoUtilities.readEventLog(calypsoPo, virtualReader, logger);
    assertThat(eventLog).isNotNull();
    remotePoolClientPlugin.releaseReader(virtualReader);
  }
}
