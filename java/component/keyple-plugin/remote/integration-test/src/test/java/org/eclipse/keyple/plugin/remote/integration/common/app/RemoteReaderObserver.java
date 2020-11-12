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
package org.eclipse.keyple.plugin.remote.integration.common.app;

import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.core.service.exception.KeypleException;
import org.eclipse.keyple.plugin.remote.integration.common.model.TransactionResult;
import org.eclipse.keyple.plugin.remote.integration.common.model.UserInput;
import org.eclipse.keyple.plugin.remote.integration.common.util.CalypsoUtilities;
import org.eclipse.keyple.plugin.remote.ObservableRemoteReaderServer;
import org.eclipse.keyple.plugin.remote.RemotePluginServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteReaderObserver implements ObservableReader.ReaderObserver {

  private static final Logger logger = LoggerFactory.getLogger(RemoteReaderObserver.class);
  private Integer eventCounter = 0;

  @Override
  public void update(ReaderEvent event) {
    String remoteReaderName = event.getReaderName();
    RemotePluginServer plugin =
        (RemotePluginServer) SmartCardService.getInstance().getPlugin(event.getPluginName());
    ObservableRemoteReaderServer observableRemoteReader =
        (ObservableRemoteReaderServer) plugin.getReader(remoteReaderName);
    logger.info(
        "Event received {} {} {} with default selection {}",
        event.getEventType(),
        event.getPluginName(),
        remoteReaderName,
        event.getDefaultSelectionsResponse());

    switch (event.getEventType()) {
      case CARD_MATCHED:
        eventCounter++;

        UserInput userInput = observableRemoteReader.getUserInputData(UserInput.class);

        // retrieve selection
        CardSelection cardSelection = CalypsoUtilities.getSeSelection();
        CalypsoPo calypsoPo =
            (CalypsoPo)
                cardSelection
                    .processDefaultSelection(event.getDefaultSelectionsResponse())
                    .getActiveSmartCard();

        // execute a transaction
        try {
          String eventLog =
              CalypsoUtilities.readEventLog(calypsoPo, observableRemoteReader, logger);
          // on the 2nd Card MATCHED
          if (eventCounter == 2) {
            // clear observers in the reader
            observableRemoteReader.clearObservers();
          }
          // send result
          plugin.terminateService(
              remoteReaderName,
              new TransactionResult()
                  .setSuccessful(!eventLog.isEmpty())
                  .setUserId(userInput.getUserId()));
        } catch (KeypleException e) {
          // send result
          plugin.terminateService(
              remoteReaderName,
              new TransactionResult().setSuccessful(false).setUserId(userInput.getUserId()));
        }

        break;
      case CARD_REMOVED:
        // do nothing
        plugin.terminateService(remoteReaderName, null);
        break;
    }
  }
}
