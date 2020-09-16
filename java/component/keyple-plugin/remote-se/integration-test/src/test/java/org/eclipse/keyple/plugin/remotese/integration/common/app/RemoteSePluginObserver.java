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

import static org.eclipse.keyple.plugin.remotese.integration.test.BaseScenario.*;

import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.plugin.remotese.integration.common.model.ConfigurationResult;
import org.eclipse.keyple.plugin.remotese.integration.common.model.DeviceInput;
import org.eclipse.keyple.plugin.remotese.integration.common.model.TransactionResult;
import org.eclipse.keyple.plugin.remotese.integration.common.model.UserInput;
import org.eclipse.keyple.plugin.remotese.integration.common.util.CalypsoUtilities;
import org.eclipse.keyple.plugin.remotese.virtualse.RemoteSeServerObservableReader;
import org.eclipse.keyple.plugin.remotese.virtualse.RemoteSeServerPlugin;
import org.eclipse.keyple.plugin.remotese.virtualse.RemoteSeServerReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteSePluginObserver implements ObservablePlugin.PluginObserver {

  private static final Logger logger = LoggerFactory.getLogger(RemoteSePluginObserver.class);

  public RemoteSePluginObserver() {}

  @Override
  public void update(PluginEvent event) {
    logger.info(
        "Event received {} {} {}",
        event.getEventType(),
        event.getPluginName(),
        event.getReaderNames().first());

    switch (event.getEventType()) {
      case READER_CONNECTED:
        // retrieve serviceId from reader
        String virtualReaderName = event.getReaderNames().first();
        RemoteSeServerPlugin plugin =
            (RemoteSeServerPlugin) SeProxyService.getInstance().getPlugin(event.getPluginName());
        RemoteSeServerReader virtualReader = plugin.getReader(virtualReaderName);

        // execute the business logic based on serviceId
        Object output = executeService(virtualReader);

        // terminate service
        plugin.terminateService(virtualReaderName, output);

        break;
    }
  }

  Object executeService(RemoteSeServerReader virtualReader) {
    logger.info("Executing ServiceId : {}", virtualReader.getServiceId());

    // "EXECUTE_CALYPSO_SESSION_FROM_LOCAL_SELECTION"
    if (SERVICE_ID_1.equals(virtualReader.getServiceId())) {
      UserInput userInput = virtualReader.getUserInputData(UserInput.class);
      CalypsoPo calypsoPo = virtualReader.getInitialSeContent(CalypsoPo.class);

      // execute a transacation
      CalypsoUtilities.readEventLog(calypsoPo, virtualReader, logger);

      return new TransactionResult().setUserId(userInput.getUserId()).setSuccessful(true);
    }

    // CREATE_CONFIGURE_OBS_VIRTUAL_READER
    if (SERVICE_ID_2.equals(virtualReader.getServiceId())) {
      RemoteSeServerObservableReader observableVirtualReader =
          (RemoteSeServerObservableReader) virtualReader;
      DeviceInput deviceInput = observableVirtualReader.getUserInputData(DeviceInput.class);

      // configure default selection on reader
      SeSelection seSelection = CalypsoUtilities.getSeSelection();
      observableVirtualReader.setDefaultSelectionRequest(
          seSelection.getSelectionOperation(),
          ObservableReader.NotificationMode.MATCHED_ONLY,
          ObservableReader.PollingMode.REPEATING);
      observableVirtualReader.startSeDetection(ObservableReader.PollingMode.REPEATING);

      // add observer
      observableVirtualReader.addObserver(new ReaderObserver());
      return new ConfigurationResult().setSuccessful(true).setDeviceId(deviceInput.getDeviceId());
    }

    throw new IllegalArgumentException("Service Id not recognized");
  }
}
