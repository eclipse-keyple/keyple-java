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
package org.eclipse.keyple.example.generic.distributed.server.webservice.server;

import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.core.card.selection.CardSelectionsResult;
import org.eclipse.keyple.core.card.selection.CardSelectionsService;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservablePlugin;
import org.eclipse.keyple.core.service.event.PluginEvent;
import org.eclipse.keyple.core.service.exception.KeypleException;
import org.eclipse.keyple.distributed.RemotePluginServer;
import org.eclipse.keyple.distributed.RemoteReaderServer;
import org.eclipse.keyple.example.generic.distributed.server.webservice.util.CalypsoTicketingService;
import org.eclipse.keyple.example.generic.distributed.server.webservice.util.UserInputDataDto;
import org.eclipse.keyple.example.generic.distributed.server.webservice.util.UserOutputDataDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example of a {@link RemotePluginServer} observer.
 *
 * <p>It contains the business logic of the remote service execution.
 */
public class RemotePluginServerObserver implements ObservablePlugin.PluginObserver {

  private static final Logger logger = LoggerFactory.getLogger(RemotePluginServerObserver.class);

  /** {@inheritDoc} */
  @Override
  public void update(PluginEvent event) {

    // For a RemotePluginServer, the events can only be of type READER_CONNECTED.
    // So there is no need to analyze the event type.
    logger.info(
        "Event received {} {} {}",
        event.getEventType(),
        event.getPluginName(),
        event.getReaderNames().first());

    // Retrieves the remote plugin using the plugin name contains in the event.
    RemotePluginServer plugin =
        (RemotePluginServer) SmartCardService.getInstance().getPlugin(event.getPluginName());

    // Retrieves the name of the remote reader using the first reader name contains in the event.
    // Note that for a RemotePluginServer, there can be only one reader per event.
    String readerName = event.getReaderNames().first();

    // Retrieves the remote reader from the plugin using the reader name.
    RemoteReaderServer reader = plugin.getReader(readerName);

    // Analyses the Service ID contains in the reader to find which business service to execute.
    // The Service ID was specified by the client when executing the remote service.
    Object userOutputData;
    if ("EXECUTE_CALYPSO_SESSION_FROM_REMOTE_SELECTION".equals(reader.getServiceId())) {

      // Executes the business service using the remote reader.
      userOutputData = executeCalypsoSessionFromRemoteSelection(reader);

    } else {
      throw new IllegalArgumentException("Service ID not recognized");
    }

    // Terminates the business service by providing the reader name and the optional output data.
    plugin.terminateService(readerName, userOutputData);
  }

  /**
   * Executes the business service having the Service ID
   * "EXECUTE_CALYPSO_SESSION_FROM_REMOTE_SELECTION".
   *
   * <p>This is an example of a ticketing transaction :
   *
   * <ol>
   *   <li>Perform a remote explicit selection,
   *   <li>Read the content of event log file.
   * </ol>
   *
   * @param reader The remote reader on where to execute the business logic.
   * @return a nullable reference to the user output data to transmit to the client.
   */
  private Object executeCalypsoSessionFromRemoteSelection(RemoteReaderServer reader) {

    // Retrieves the optional userInputData specified by the client when executing the remote
    // service.
    UserInputDataDto userInputData = reader.getUserInputData(UserInputDataDto.class);

    // Performs a remote explicit selection.
    CardSelectionsService cardSelection = CalypsoTicketingService.getCardSelection();
    CardSelectionsResult selectionsResult = cardSelection.processExplicitSelections(reader);
    CalypsoPo calypsoPo = (CalypsoPo) selectionsResult.getActiveSmartCard();

    // Read the content of event log file.
    try {
      CalypsoTicketingService.readEventLog(calypsoPo, reader);

      // Return a successful transaction result.
      return new UserOutputDataDto().setUserId(userInputData.getUserId()).setSuccessful(true);

    } catch (KeypleException e) {
      // If an exception is thrown, then return an unsuccessful transaction result.
      return new UserOutputDataDto().setUserId(userInputData.getUserId()).setSuccessful(false);
    }
  }
}
