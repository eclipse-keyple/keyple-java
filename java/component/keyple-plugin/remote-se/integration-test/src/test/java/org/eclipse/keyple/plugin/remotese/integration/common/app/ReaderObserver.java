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
import org.eclipse.keyple.plugin.remotese.integration.common.model.TransactionResult;
import org.eclipse.keyple.plugin.remotese.integration.common.model.UserInput;
import org.eclipse.keyple.plugin.remotese.integration.common.util.CalypsoUtilities;
import org.eclipse.keyple.plugin.remotese.virtualse.RemoteSeServerObservableReader;
import org.eclipse.keyple.plugin.remotese.virtualse.RemoteSeServerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReaderObserver implements ObservableReader.ReaderObserver {

  private static final Logger logger = LoggerFactory.getLogger(ReaderObserver.class);

  @Override
  public void update(ReaderEvent event) {
    logger.info(
        "Event received {} {} {} with default selection {}",
        event.getEventType(),
        event.getPluginName(),
        event.getReader(),
        event.getDefaultSelectionsResponse());

    switch (event.getEventType()) {
      case SE_MATCHED:
        String virtualReaderName = event.getReaderName();
        RemoteSeServerPlugin plugin =
            (RemoteSeServerPlugin) SeProxyService.getInstance().getPlugin(event.getPluginName());
        RemoteSeServerObservableReader observableVirtualReader =
            (RemoteSeServerObservableReader) plugin.getReader(virtualReaderName);
        UserInput userInput = observableVirtualReader.getUserInputData(UserInput.class);

        // retrieve selection
        SeSelection seSelection = CalypsoUtilities.getSeSelection();
        CalypsoPo calypsoPo =
            (CalypsoPo)
                seSelection
                    .processDefaultSelection(event.getDefaultSelectionsResponse())
                    .getActiveMatchingSe();

        // execute a transaction
        String eventLog = CalypsoUtilities.readEventLog(calypsoPo, observableVirtualReader, logger);

        // send result
        plugin.terminateService(
            virtualReaderName,
            new TransactionResult()
                .setSuccessful(!eventLog.isEmpty())
                .setUserId(userInput.getUserId()));
        break;
    }
  }
}
