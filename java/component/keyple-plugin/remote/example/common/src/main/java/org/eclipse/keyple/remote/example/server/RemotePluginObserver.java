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
package org.eclipse.keyple.remote.example.server;

import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.card.selection.SelectionsResult;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservablePlugin;
import org.eclipse.keyple.core.service.event.PluginEvent;
import org.eclipse.keyple.core.service.exception.KeypleException;
import org.eclipse.keyple.plugin.remote.RemotePluginServer;
import org.eclipse.keyple.plugin.remote.RemoteReaderServer;
import org.eclipse.keyple.remote.example.common.TransactionResult;
import org.eclipse.keyple.remote.example.common.UserInfo;
import org.eclipse.keyple.remote.example.util.CalypsoTicketingLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example of a PluginObserver for a {@link RemotePluginServer}. It contains the business logic of
 * the remote service execution
 */
public class RemotePluginObserver implements ObservablePlugin.PluginObserver {

  private static final Logger logger = LoggerFactory.getLogger(RemotePluginObserver.class);

  @Override
  public void update(PluginEvent event) {
    logger.info(
        "Event received {} {} {}",
        event.getEventType(),
        event.getPluginName(),
        event.getReaderNames().first());

    switch (event.getEventType()) {
      case READER_CONNECTED:
        // retrieve plugin
        RemotePluginServer plugin =
            (RemotePluginServer) SmartCardService.getInstance().getPlugin(event.getPluginName());

        // retrieve reader
        String remoteReaderName = event.getReaderNames().first();
        RemoteReaderServer remoteReader = plugin.getReader(remoteReaderName);

        // execute the business logic based on serviceId
        Object output = executeService(remoteReader);

        // terminate service
        plugin.terminateService(remoteReaderName, output);

        break;
    }
  }

  /**
   * Execute a service based on the serviceId of the virtual reader
   *
   * @param remoteReader the virtual reader on where to execute the business logic
   * @return output object
   */
  private Object executeService(RemoteReaderServer remoteReader) {

    /*
     * Retrieve the serviceId specified by the client when executing the remote service. Based on this serviceId, the server can select the ticketing logic to execute.
     */
    final String serviceId = remoteReader.getServiceId();
    logger.info("Executing ServiceId : {}", serviceId);

    // the service Id EXECUTE_CALYPSO_SESSION_FROM_REMOTE_SELECTION matches the following logic
    if ("EXECUTE_CALYPSO_SESSION_FROM_REMOTE_SELECTION".equals(serviceId)) {
      /*
       * Retrieve the userInputData specified by the client when executing the remote service.
       */
      UserInfo userInput = remoteReader.getUserInputData(UserInfo.class);

      /*
       * Execute an example of a ticketing transaction :
       * - perform a remote explicit selection
       * - read the content of event log file
       */
      // perform a remote explicit selection
      CardSelection seSelection = CalypsoTicketingLogic.getSeSelection();
      SelectionsResult selectionsResult = seSelection.processExplicitSelection(remoteReader);
      CalypsoPo calypsoPo = (CalypsoPo) selectionsResult.getActiveSmartCard();

      try {
        // read the content of event log file
        CalypsoTicketingLogic.readEventLog(calypsoPo, remoteReader);
        // return a successful transaction result
        return new TransactionResult().setUserId(userInput.getUserId()).setSuccessful(true);
      } catch (KeypleException e) {
        // if an exception is thrown, return an unsuccessful transaction result
        return new TransactionResult().setSuccessful(false).setUserId(userInput.getUserId());
      }
    }

    throw new IllegalArgumentException("Service Id not recognized");
  }
}
