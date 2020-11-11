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

import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.card.selection.SelectionsResult;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservablePlugin;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.PluginEvent;
import org.eclipse.keyple.core.service.exception.KeypleException;
import org.eclipse.keyple.plugin.remote.integration.common.model.ConfigurationResult;
import org.eclipse.keyple.plugin.remote.integration.common.model.DeviceInput;
import org.eclipse.keyple.plugin.remote.integration.common.model.TransactionResult;
import org.eclipse.keyple.plugin.remote.integration.common.model.UserInput;
import org.eclipse.keyple.plugin.remote.integration.common.util.CalypsoUtilities;
import org.eclipse.keyple.plugin.remote.integration.service.BaseScenario;
import org.eclipse.keyple.plugin.remote.RemoteServerObservableReader;
import org.eclipse.keyple.plugin.remote.RemoteServerPlugin;
import org.eclipse.keyple.plugin.remote.RemoteServerReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemotePluginObserver implements ObservablePlugin.PluginObserver {

  private static final Logger logger = LoggerFactory.getLogger(RemotePluginObserver.class);

  public RemotePluginObserver() {}

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
        RemoteServerPlugin plugin =
            (RemoteServerPlugin) SmartCardService.getInstance().getPlugin(event.getPluginName());
        RemoteServerReader virtualReader = plugin.getReader(virtualReaderName);

        // execute the business logic based on serviceId
        Object output = executeService(virtualReader);

        // terminate service
        plugin.terminateService(virtualReaderName, output);

        break;
    }
  }

  Object executeService(RemoteServerReader virtualReader) {
    logger.info("Executing ServiceId : {}", virtualReader.getServiceId());

    // "EXECUTE_CALYPSO_SESSION_FROM_LOCAL_SELECTION"
    if (BaseScenario.SERVICE_ID_1.equals(virtualReader.getServiceId())) {

      CalypsoPo calypsoPo = virtualReader.getInitialCardContent(CalypsoPo.class);
      UserInput userInput = virtualReader.getUserInputData(UserInput.class);
      try {
        // execute a transaction
        CalypsoUtilities.readEventLog(calypsoPo, virtualReader, logger);
        return new TransactionResult().setUserId(userInput.getUserId()).setSuccessful(true);
      } catch (KeypleException e) {
        return new TransactionResult().setSuccessful(false).setUserId(userInput.getUserId());
      }
    }

    // CREATE_CONFIGURE_OBS_VIRTUAL_READER
    if (BaseScenario.SERVICE_ID_2.equals(virtualReader.getServiceId())) {
      RemoteServerObservableReader observableVirtualReader =
          (RemoteServerObservableReader) virtualReader;
      DeviceInput deviceInput = observableVirtualReader.getUserInputData(DeviceInput.class);

      // configure default selection on reader
      CardSelection cardSelection = CalypsoUtilities.getSeSelection();
      observableVirtualReader.setDefaultSelectionRequest(
          cardSelection.getSelectionOperation(),
          ObservableReader.NotificationMode.MATCHED_ONLY,
          ObservableReader.PollingMode.REPEATING);

      // add observer
      observableVirtualReader.addObserver(new VirtualReaderObserver());
      return new ConfigurationResult().setSuccessful(true).setDeviceId(deviceInput.getDeviceId());
    }

    // EXECUTE_CALYPSO_SESSION_FROM_REMOTE_SELECTION
    if (BaseScenario.SERVICE_ID_3.equals(virtualReader.getServiceId())) {
      UserInput userInput = virtualReader.getUserInputData(UserInput.class);

      // remote selection
      CardSelection cardSelection = CalypsoUtilities.getSeSelection();
      SelectionsResult selectionsResult = cardSelection.processExplicitSelection(virtualReader);
      CalypsoPo calypsoPo = (CalypsoPo) selectionsResult.getActiveSmartCard();

      try {
        // execute a transaction
        CalypsoUtilities.readEventLog(calypsoPo, virtualReader, logger);
        return new TransactionResult().setUserId(userInput.getUserId()).setSuccessful(true);
      } catch (KeypleException e) {
        return new TransactionResult().setSuccessful(false).setUserId(userInput.getUserId());
      }
    }

    // EXECUTE ALL METHODS
    if (BaseScenario.SERVICE_ID_4.equals(virtualReader.getServiceId())) {

      RemoteServerObservableReader observableVirtualReader =
          (RemoteServerObservableReader) virtualReader;
      DeviceInput deviceInput = observableVirtualReader.getUserInputData(DeviceInput.class);

      observableVirtualReader.startCardDetection(ObservableReader.PollingMode.REPEATING);

      if (!observableVirtualReader.isCardPresent()) {
        throw new IllegalStateException("Card should be inserted");
      }

      if (!observableVirtualReader.isContactless()) {
        throw new IllegalStateException("Reader should be contactless");
      }
      ;
      observableVirtualReader.stopCardDetection();
      return new ConfigurationResult().setDeviceId(deviceInput.getDeviceId()).setSuccessful(true);
    }

    throw new IllegalArgumentException("Service Id not recognized");
  }
}