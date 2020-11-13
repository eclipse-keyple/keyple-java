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
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.exception.KeypleException;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.plugin.pcsc.PcscSupportedProtocols;
import org.eclipse.keyple.plugin.remote.LocalServiceClient;
import org.eclipse.keyple.plugin.remote.PoolRemotePluginClient;
import org.eclipse.keyple.plugin.remote.RemoteServiceParameters;
import org.eclipse.keyple.plugin.remote.impl.LocalServiceClientFactory;
import org.eclipse.keyple.plugin.remote.impl.PoolRemotePluginClientFactory;
import org.eclipse.keyple.plugin.remote.impl.PoolRemotePluginClientUtils;
import org.eclipse.keyple.plugin.stub.*;
import org.eclipse.keyple.remote.example.card.CalypsoTicketingLogic;
import org.eclipse.keyple.remote.example.model.TransactionResult;
import org.eclipse.keyple.remote.example.model.UserInfo;
import org.eclipse.keyple.remote.example.card.StubCalypsoClassic;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Example of a client side app */
@ApplicationScoped
public class ClientApp {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientApp.class);

  @Inject @RestClient
  WebserviceEndpointClient clientEndpoint;

  String REFERENCE_GROUP = "group1";

  /**
   *
   */
  public void init() {

      SmartCardService.getInstance().registerPlugin(
              PoolRemotePluginClientFactory
                      .builder()
                      .withSyncNode(clientEndpoint)
                      .usingDefaultTimeout()
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
