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
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.plugin.remote.integration.common.endpoint.pool.StubAsyncClientEndpoint;
import org.eclipse.keyple.plugin.remote.integration.common.endpoint.pool.StubAsyncServerEndpoint;
import org.eclipse.keyple.plugin.remote.integration.common.util.CalypsoUtilities;
import org.eclipse.keyple.plugin.remote.nativ.impl.NativePoolServerServiceFactory;
import org.eclipse.keyple.plugin.remote.virtual.RemotePoolClientPlugin;
import org.eclipse.keyple.plugin.remote.virtual.impl.RemotePoolClientPluginFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncScenario extends BaseScenario {

  private static final Logger logger = LoggerFactory.getLogger(AsyncScenario.class);

  @Before
  public void setUp() {
    initNativePoolStubPlugin();

    StubAsyncServerEndpoint serverEndpoint = new StubAsyncServerEndpoint();
    StubAsyncClientEndpoint clientEndpoint = new StubAsyncClientEndpoint(serverEndpoint);

    nativePoolServerService =
        new NativePoolServerServiceFactory()
            .builder()
            .withAsyncNode(serverEndpoint)
            .withPoolPlugins(nativePoolPlugin.getName())
            .getService();

    remotePoolClientPlugin =
        (RemotePoolClientPlugin)
            SeProxyService.getInstance()
                .registerPlugin(
                    RemotePoolClientPluginFactory.builder()
                        .withAsyncNode(clientEndpoint)
                        .usingDefaultTimeout()
                        .build());
  }

  @After
  public void tearDown() {
    SeProxyService.getInstance().unregisterPlugin(remotePoolClientPlugin.getName());
  }

  @Test
  @Override
  public void execute_transaction_on_pool_reader() {
    SortedSet<String> groupReferences = remotePoolClientPlugin.getReaderGroupReferences();
    assertThat(groupReferences).containsExactly(groupReference);

    SeReader virtualReader = remotePoolClientPlugin.allocateReader(groupReference);
    SeSelection seSelection = CalypsoUtilities.getSeSelection();
    CalypsoPo calypsoPo =
        (CalypsoPo) seSelection.processExplicitSelection(virtualReader).getActiveMatchingSe();

    String eventLog = CalypsoUtilities.readEventLog(calypsoPo, virtualReader, logger);
    assertThat(eventLog).isNotNull();
    remotePoolClientPlugin.releaseReader(virtualReader);
  }
}
