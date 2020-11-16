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
package org.eclipse.keyple.example.calypso.remote.webservice.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.card.selection.SelectionsResult;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.example.calypso.remote.webservice.util.CalypsoTicketingLogic;
import org.eclipse.keyple.plugin.remote.PoolRemotePluginClient;
import org.eclipse.keyple.plugin.remote.impl.PoolRemotePluginClientFactory;
import org.eclipse.keyple.plugin.remote.impl.PoolRemotePluginClientUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client app for the {@link PoolRemotePluginClient} example. Execute the transaction on a remote reader.
 */
@ApplicationScoped
public class ClientApp {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientApp.class);

  @Inject @RestClient
  PoolLocalServiceClient clientEndpoint;

  String REFERENCE_GROUP = "group1";

  /**
   * Initialize the {@link PoolRemotePluginClient}
   */
  public void init() {

      SmartCardService.getInstance().registerPlugin(
              PoolRemotePluginClientFactory
                      .builder()
                      .withSyncNode(clientEndpoint) //use the web service client endpoint configured to communicate with the server where is located the local pool plugin
                      .usingDefaultTimeout() //use a default timeout
                      .build());

  }

  /**
   * Execute a simple scenario : allocate a reader, execute a transaction, release reader
   *
   * @return true if the transaction was successful
   */
  public Boolean launchScenario() {
    PoolRemotePluginClient poolRemotePlugin = PoolRemotePluginClientUtils.getRemotePlugin();

    /*
     * Allocate a remote reader
     */
    Reader remoteReader = poolRemotePlugin.allocateReader(REFERENCE_GROUP);

    /*
     * Execute an example of a ticketing transaction :
     * - perform a remote explicit selection
     * - read the content of event log file
     */
    CardSelection seSelection = CalypsoTicketingLogic.getSeSelection();
    SelectionsResult selectionsResult = seSelection.processExplicitSelection(remoteReader);
    CalypsoPo calypsoPo = (CalypsoPo) selectionsResult.getActiveSmartCard();

    /*
     * read the content of event log file
     */
    String eventLog = CalypsoTicketingLogic.readEventLog(calypsoPo, remoteReader);

    /*
     * Release a remote reader
     */
    poolRemotePlugin.releaseReader(remoteReader);

    return eventLog != null;
  }
}
