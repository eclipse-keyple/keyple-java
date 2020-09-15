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

import static org.eclipse.keyple.plugin.remotese.integration.test.BaseScenario.SERVICE_ID_1;

import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.selection.SeResource;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.plugin.remotese.integration.common.model.UserInput;
import org.eclipse.keyple.plugin.remotese.integration.common.model.UserOutput;
import org.eclipse.keyple.plugin.remotese.integration.common.util.CalypsoClassicInfo;
import org.eclipse.keyple.plugin.remotese.virtualse.RemoteSeServerPlugin;
import org.eclipse.keyple.plugin.remotese.virtualse.RemoteSeServerReader;
import org.eclipse.keyple.plugin.remotese.virtualse.impl.RemoteSeServerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteSePluginObserver implements ObservablePlugin.PluginObserver {

  private static final Logger logger = LoggerFactory.getLogger(RemoteSePluginObserver.class);

  boolean isSync;

  public RemoteSePluginObserver(boolean isSync) {
    this.isSync = isSync;
  }

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
            isSync ? RemoteSeServerUtils.getSyncPlugin() : RemoteSeServerUtils.getAsyncPlugin();
        RemoteSeServerReader virtualReader = plugin.getReader(virtualReaderName);

        // execute the business logic based on serviceId
        UserOutput output = executeService(virtualReader);

        // terminate service
        plugin.terminateService(virtualReaderName, output);

        break;
    }
  }

  UserOutput executeService(RemoteSeServerReader virtualReader) {
    logger.info("Executing ServiceId : {}", virtualReader.getServiceId());
    if (SERVICE_ID_1.equals(virtualReader.getServiceId())) {

      UserInput userInput = virtualReader.getUserInputData(UserInput.class);
      CalypsoPo calypsoPo = virtualReader.getInitialSeContent(CalypsoPo.class);
      // execute calypso session from a se selection
      logger.info(
          "Initial PO Content, atr : {}, sn : {}",
          calypsoPo.getAtr(),
          calypsoPo.getApplicationSerialNumber());

      // Retrieve the data read from the CalyspoPo updated during the transaction process
      ElementaryFile efEnvironmentAndHolder =
          calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_EnvironmentAndHolder);
      String environmentAndHolder =
          ByteArrayUtil.toHex(efEnvironmentAndHolder.getData().getContent());

      // Log the result
      logger.info("EnvironmentAndHolder file data: {}", environmentAndHolder);

      // Go on with the reading of the first record of the EventLog file
      logger.info("= #### reading transaction of the EventLog file.");

      PoTransaction poTransaction =
          new PoTransaction(new SeResource<CalypsoPo>(virtualReader, calypsoPo));

      // Prepare the reading order and keep the associated parser for later use once the
      // transaction has been processed.
      poTransaction.prepareReadRecordFile(
          CalypsoClassicInfo.SFI_EventLog, CalypsoClassicInfo.RECORD_NUMBER_1);

      // Actual PO communication: send the prepared read order, then close the channel with
      // the PO
      poTransaction.prepareReleasePoChannel();
      poTransaction.processPoCommands();
      logger.info("The reading of the EventLog has succeeded.");

      // Retrieve the data read from the CalyspoPo updated during the transaction process
      ElementaryFile efEventLog = calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_EventLog);
      String eventLog = ByteArrayUtil.toHex(efEventLog.getData().getContent());

      // Log the result
      logger.info("EventLog file data: {}", eventLog);

      return new UserOutput().setUserId(userInput.getUserId()).setSuccessful(true);
    }
    throw new IllegalArgumentException("Service Id not recognized");
  }
}
