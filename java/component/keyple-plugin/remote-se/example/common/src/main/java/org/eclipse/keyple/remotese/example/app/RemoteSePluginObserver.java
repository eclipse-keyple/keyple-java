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
package org.eclipse.keyple.remotese.example.app;

import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.selection.SeResource;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.selection.SelectionsResult;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.plugin.remotese.virtualse.RemoteSeServerPlugin;
import org.eclipse.keyple.plugin.remotese.virtualse.RemoteSeServerReader;
import org.eclipse.keyple.remotese.example.model.TransactionResult;
import org.eclipse.keyple.remotese.example.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example of a PluginObserver for a {@link RemoteSeServerPlugin}. It contains the business logic of
 * the remote service execution
 */
public class RemoteSePluginObserver implements ObservablePlugin.PluginObserver {

  /** AID: Keyple */
  public static final String AID = "315449432E49434131";

  public static final byte RECORD_NUMBER_1 = 1;
  public static final byte SFI_EnvironmentAndHolder = (byte) 0x07;
  public static final byte SFI_EventLog = (byte) 0x08;
  private static final Logger logger = LoggerFactory.getLogger(RemoteSePluginObserver.class);

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

  /**
   * Execute a service based on the serviceId of the virtual reader
   *
   * @param virtualReader the virtual reader on where to execute the business logic
   * @return output object
   */
  private Object executeService(RemoteSeServerReader virtualReader) {
    logger.info("Executing ServiceId : {}", virtualReader.getServiceId());

    // EXECUTE_CALYPSO_SESSION_FROM_REMOTE_SELECTION
    if ("EXECUTE_CALYPSO_SESSION_FROM_REMOTE_SELECTION".equals(virtualReader.getServiceId())) {
      UserInfo userInput = virtualReader.getUserInputData(UserInfo.class);

      // perform a remote explicit SE selection
      SeSelection seSelection = getSeSelection();
      SelectionsResult selectionsResult = seSelection.processExplicitSelection(virtualReader);
      CalypsoPo calypsoPo = (CalypsoPo) selectionsResult.getActiveMatchingSe();

      try {
        // execute a transaction
        readEventLog(calypsoPo, virtualReader);
        return new TransactionResult().setUserId(userInput.getUserId()).setSuccessful(true);
      } catch (KeypleException e) {
        return new TransactionResult().setSuccessful(false).setUserId(userInput.getUserId());
      }
    }

    throw new IllegalArgumentException("Service Id not recognized");
  }

  /**
   * Prepare a SE Selection object ready to select Calypso SE and read environment file
   *
   * @return instance of SE Selection object
   */
  private SeSelection getSeSelection() {
    // Prepare PO Selection
    SeSelection seSelection = new SeSelection();

    // Calypso selection
    PoSelectionRequest poSelectionRequest =
        new PoSelectionRequest(
            PoSelector.builder()
                .seProtocol(SeCommonProtocols.PROTOCOL_ISO14443_4)
                .aidSelector(SeSelector.AidSelector.builder().aidToSelect(AID).build())
                .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                .build());

    // Prepare the reading order.
    poSelectionRequest.prepareReadRecordFile(SFI_EnvironmentAndHolder, RECORD_NUMBER_1);

    // Add the selection case to the current selection
    seSelection.prepareSelection(poSelectionRequest);
    return seSelection;
  }

  /**
   * Read and return content of event log file within a Portable Object Transaction
   *
   * @param calypsoPo smartcard to read to the event log file
   * @param seReader native reader where the smartcard is inserted
   * @return content of the event log file in Hexadecimal
   */
  private String readEventLog(CalypsoPo calypsoPo, SeReader seReader) {
    // execute calypso session from a se selection
    logger.info(
        "Initial PO Content, atr : {}, sn : {}",
        calypsoPo.getAtr(),
        calypsoPo.getApplicationSerialNumber());

    // Retrieve the data read from the CalyspoPo updated during the transaction process
    ElementaryFile efEnvironmentAndHolder = calypsoPo.getFileBySfi(SFI_EnvironmentAndHolder);
    String environmentAndHolder =
        ByteArrayUtil.toHex(efEnvironmentAndHolder.getData().getContent());

    // Log the result
    logger.info("EnvironmentAndHolder file data: {}", environmentAndHolder);

    // Go on with the reading of the first record of the EventLog file
    logger.info("= #### reading transaction of the EventLog file.");

    PoTransaction poTransaction = new PoTransaction(new SeResource<CalypsoPo>(seReader, calypsoPo));

    // Prepare the reading order and keep the associated parser for later use once the
    // transaction has been processed.
    poTransaction.prepareReadRecordFile(SFI_EventLog, RECORD_NUMBER_1);

    // Actual PO communication: send the prepared read order, then close the channel with
    // the PO
    poTransaction.prepareReleasePoChannel();
    poTransaction.processPoCommands();
    logger.info("The reading of the EventLog has succeeded.");

    // Retrieve the data read from the CalyspoPo updated during the transaction process
    ElementaryFile efEventLog = calypsoPo.getFileBySfi(SFI_EventLog);
    String eventLog = ByteArrayUtil.toHex(efEventLog.getData().getContent());

    // Log the result
    logger.info("EventLog file data: {}", eventLog);

    return eventLog;
  }
}
