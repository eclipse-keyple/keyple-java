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
import org.eclipse.keyple.plugin.remote.ObservableRemoteReaderServer;
import org.eclipse.keyple.plugin.remote.RemotePluginServer;
import org.eclipse.keyple.plugin.remote.RemoteReaderServer;
import org.eclipse.keyple.plugin.remote.integration.common.model.ConfigurationResult;
import org.eclipse.keyple.plugin.remote.integration.common.model.DeviceInput;
import org.eclipse.keyple.plugin.remote.integration.common.model.UserInput;
import org.eclipse.keyple.plugin.remote.integration.common.model.UserOutputDataDto;
import org.eclipse.keyple.plugin.remote.integration.common.util.CalypsoUtilities;
import org.eclipse.keyple.plugin.remote.integration.service.BaseScenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemotePluginServerObserver implements ObservablePlugin.PluginObserver {

  private static final Logger logger = LoggerFactory.getLogger(RemotePluginServerObserver.class);

  public RemotePluginServerObserver() {}

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
        String remoteReaderName = event.getReaderNames().first();
        RemotePluginServer plugin =
            (RemotePluginServer) SmartCardService.getInstance().getPlugin(event.getPluginName());
        RemoteReaderServer remoteReader = plugin.getReader(remoteReaderName);

        // execute the business logic based on serviceId
        Object output = executeService(remoteReader);

        // terminate service
        plugin.terminateService(remoteReaderName, output);

        break;
    }
  }

  Object executeService(RemoteReaderServer remoteReader) {
    logger.info("Executing ServiceId : {}", remoteReader.getServiceId());

    // "EXECUTE_CALYPSO_SESSION_FROM_LOCAL_SELECTION"
    if (BaseScenario.SERVICE_ID_1.equals(remoteReader.getServiceId())) {

      CalypsoPo calypsoPo = remoteReader.getInitialCardContent(CalypsoPo.class);
      UserInput userInput = remoteReader.getUserInputData(UserInput.class);
      try {
        // execute a transaction
        CalypsoUtilities.readEventLog(calypsoPo, remoteReader, logger);
        return new UserOutputDataDto().setUserId(userInput.getUserId()).setSuccessful(true);
      } catch (KeypleException e) {
        return new UserOutputDataDto().setSuccessful(false).setUserId(userInput.getUserId());
      }
    }

    // CREATE_CONFIGURE_OBS_VIRTUAL_READER
    if (BaseScenario.SERVICE_ID_2.equals(remoteReader.getServiceId())) {
      ObservableRemoteReaderServer observableRemoteReader =
          (ObservableRemoteReaderServer) remoteReader;
      DeviceInput deviceInput = observableRemoteReader.getUserInputData(DeviceInput.class);

      // configure default selection on reader
      CardSelection cardSelection = CalypsoUtilities.getSeSelection();
      observableRemoteReader.setDefaultSelectionRequest(
          cardSelection.getSelectionOperation(),
          ObservableReader.NotificationMode.MATCHED_ONLY,
          ObservableReader.PollingMode.REPEATING);

      // add observer
      observableRemoteReader.addObserver(new RemoteReaderServerObserver());
      return new ConfigurationResult().setSuccessful(true).setDeviceId(deviceInput.getDeviceId());
    }

    // EXECUTE_CALYPSO_SESSION_FROM_REMOTE_SELECTION
    if (BaseScenario.SERVICE_ID_3.equals(remoteReader.getServiceId())) {
      UserInput userInput = remoteReader.getUserInputData(UserInput.class);

      // remote selection
      CardSelection cardSelection = CalypsoUtilities.getSeSelection();
      SelectionsResult selectionsResult = cardSelection.processExplicitSelection(remoteReader);
      CalypsoPo calypsoPo = (CalypsoPo) selectionsResult.getActiveSmartCard();

      try {
        // execute a transaction
        CalypsoUtilities.readEventLog(calypsoPo, remoteReader, logger);
        return new UserOutputDataDto().setUserId(userInput.getUserId()).setSuccessful(true);
      } catch (KeypleException e) {
        return new UserOutputDataDto().setSuccessful(false).setUserId(userInput.getUserId());
      }
    }

    // EXECUTE ALL METHODS
    if (BaseScenario.SERVICE_ID_4.equals(remoteReader.getServiceId())) {

      ObservableRemoteReaderServer observableRemoteReader =
          (ObservableRemoteReaderServer) remoteReader;
      DeviceInput deviceInput = observableRemoteReader.getUserInputData(DeviceInput.class);

      observableRemoteReader.startCardDetection(ObservableReader.PollingMode.REPEATING);

      if (!observableRemoteReader.isCardPresent()) {
        throw new IllegalStateException("Card should be inserted");
      }

      if (!observableRemoteReader.isContactless()) {
        throw new IllegalStateException("Reader should be contactless");
      }
      ;
      observableRemoteReader.stopCardDetection();
      return new ConfigurationResult().setDeviceId(deviceInput.getDeviceId()).setSuccessful(true);
    }

    throw new IllegalArgumentException("Service Id not recognized");
  }
}
