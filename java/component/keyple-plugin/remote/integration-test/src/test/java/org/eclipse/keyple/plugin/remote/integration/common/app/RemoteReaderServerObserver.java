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
import org.eclipse.keyple.core.card.selection.CardSelectionsService;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.core.service.exception.KeypleException;
import org.eclipse.keyple.plugin.remote.ObservableRemoteReaderServer;
import org.eclipse.keyple.plugin.remote.RemotePluginServer;
import org.eclipse.keyple.plugin.remote.integration.common.model.UserInput;
import org.eclipse.keyple.plugin.remote.integration.common.model.UserOutputDataDto;
import org.eclipse.keyple.plugin.remote.integration.common.util.CalypsoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteReaderServerObserver implements ObservableReader.ReaderObserver {

  private static final Logger logger = LoggerFactory.getLogger(RemoteReaderServerObserver.class);
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
        CardSelectionsService cardSelection = CalypsoUtils.getCardSelection();
        CalypsoPo calypsoPo =
            (CalypsoPo)
                cardSelection
                    .processDefaultSelectionsResponse(event.getDefaultSelectionsResponse())
                    .getActiveSmartCard();

        // execute a transaction
        try {
          String eventLog = CalypsoUtils.readEventLog(calypsoPo, observableRemoteReader, logger);
          // on the 2nd Card MATCHED
          if (eventCounter == 2) {
            // clear observers in the reader
            observableRemoteReader.clearObservers();
          }
          // send result
          plugin.terminateService(
              remoteReaderName,
              new UserOutputDataDto()
                  .setSuccessful(!eventLog.isEmpty())
                  .setUserId(userInput.getUserId()));
        } catch (KeypleException e) {
          // send result
          plugin.terminateService(
              remoteReaderName,
              new UserOutputDataDto().setSuccessful(false).setUserId(userInput.getUserId()));
        }

        break;
      case CARD_REMOVED:
        // do nothing
        plugin.terminateService(remoteReaderName, null);
        break;
    }
  }
}
