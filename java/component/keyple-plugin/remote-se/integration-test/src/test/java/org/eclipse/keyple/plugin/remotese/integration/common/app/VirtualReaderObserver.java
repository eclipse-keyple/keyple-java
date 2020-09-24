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
package org.eclipse.keyple.plugin.remotese.integration.common.app;

import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.plugin.remotese.integration.common.model.TransactionResult;
import org.eclipse.keyple.plugin.remotese.integration.common.model.UserInput;
import org.eclipse.keyple.plugin.remotese.integration.common.util.CalypsoUtilities;
import org.eclipse.keyple.plugin.remotese.virtualse.RemoteSeServerObservableReader;
import org.eclipse.keyple.plugin.remotese.virtualse.RemoteSeServerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualReaderObserver implements ObservableReader.ReaderObserver {

  private static final Logger logger = LoggerFactory.getLogger(VirtualReaderObserver.class);
  private Integer eventCounter = 0;

  @Override
  public void update(ReaderEvent event) {
    String virtualReaderName = event.getReaderName();
    RemoteSeServerPlugin plugin =
        (RemoteSeServerPlugin) SeProxyService.getInstance().getPlugin(event.getPluginName());
    RemoteSeServerObservableReader observableVirtualReader =
        (RemoteSeServerObservableReader) plugin.getReader(virtualReaderName);
    logger.info(
        "Event received {} {} {} with default selection {}",
        event.getEventType(),
        event.getPluginName(),
        virtualReaderName,
        event.getDefaultSelectionsResponse());

    switch (event.getEventType()) {
      case SE_MATCHED:
        eventCounter++;

        UserInput userInput = observableVirtualReader.getUserInputData(UserInput.class);

        // retrieve selection
        SeSelection seSelection = CalypsoUtilities.getSeSelection();
        CalypsoPo calypsoPo =
            (CalypsoPo)
                seSelection
                    .processDefaultSelection(event.getDefaultSelectionsResponse())
                    .getActiveMatchingSe();

        // execute a transaction
        try {
          String eventLog =
              CalypsoUtilities.readEventLog(calypsoPo, observableVirtualReader, logger);
          // on the 2nd SE MATCHED
          if (eventCounter == 2) {
            // clear observers in the reader
            observableVirtualReader.clearObservers();
          }
          // send result
          plugin.terminateService(
              virtualReaderName,
              new TransactionResult()
                  .setSuccessful(!eventLog.isEmpty())
                  .setUserId(userInput.getUserId()));
        } catch (KeypleException e) {
          // send result
          plugin.terminateService(
              virtualReaderName,
              new TransactionResult().setSuccessful(false).setUserId(userInput.getUserId()));
        }

        break;
      case SE_REMOVED:
        // do nothing
        plugin.terminateService(virtualReaderName, null);
        break;
    }
  }
}
