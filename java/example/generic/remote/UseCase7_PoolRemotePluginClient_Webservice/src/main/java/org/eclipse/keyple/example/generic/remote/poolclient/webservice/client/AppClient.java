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
package org.eclipse.keyple.example.generic.remote.poolclient.webservice.client;

import java.util.SortedSet;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.core.card.selection.CardSelectionsResult;
import org.eclipse.keyple.core.card.selection.CardSelectionsService;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.example.generic.remote.poolclient.webservice.util.CalypsoTicketingService;
import org.eclipse.keyple.plugin.remote.PoolRemotePluginClient;
import org.eclipse.keyple.plugin.remote.impl.PoolRemotePluginClientFactory;
import org.eclipse.keyple.plugin.remote.impl.PoolRemotePluginClientUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/** Example of a client side application. */
@ApplicationScoped
public class AppClient {

  /** The endpoint client */
  @Inject @RestClient EndpointClient endpointClient;

  /**
   * Initialize the client components :
   *
   * <ul>
   *   <li>A {@link org.eclipse.keyple.plugin.remote.PoolRemotePluginClient} with a sync node.
   * </ul>
   */
  public void init() {

    // Init the remote plugin factory.
    PoolRemotePluginClientFactory factory =
        PoolRemotePluginClientFactory.builder().withSyncNode(endpointClient).build();

    // Register the remote plugin to the smart card service using the factory.
    SmartCardService.getInstance().registerPlugin(factory);
  }

  /**
   * Executes a simple scenario : allocate a reader, execute a transaction, release the reader.
   *
   * @return true if the transaction was successful
   */
  public Boolean launchScenario() {

    // Retrieves the pool remote plugin.
    PoolRemotePluginClient poolRemotePlugin = PoolRemotePluginClientUtils.getRemotePlugin();

    // Retrieves the reader group references available.
    SortedSet<String> groupReferences = poolRemotePlugin.getReaderGroupReferences();

    // Allocates a remote reader.
    Reader remoteReader = poolRemotePlugin.allocateReader(groupReferences.first());

    // Execute a ticketing transaction :
    // 1. perform a remote explicit selection
    CardSelectionsService cardSelection = CalypsoTicketingService.getCardSelection();
    CardSelectionsResult selectionsResult = cardSelection.processExplicitSelections(remoteReader);

    // 2. Reads the content of event log file
    CalypsoPo calypsoPo = (CalypsoPo) selectionsResult.getActiveSmartCard();
    String eventLog = CalypsoTicketingService.readEventLog(calypsoPo, remoteReader);

    // Releases the remote reader.
    poolRemotePlugin.releaseReader(remoteReader);

    return !eventLog.isEmpty();
  }
}
