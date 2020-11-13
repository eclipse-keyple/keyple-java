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
package org.eclipse.keyple.remote.example.app;

import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.card.selection.SelectionsResult;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservablePlugin;
import org.eclipse.keyple.core.service.event.PluginEvent;
import org.eclipse.keyple.core.service.exception.KeypleException;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.plugin.remote.RemotePluginServer;
import org.eclipse.keyple.plugin.remote.RemoteReaderServer;
import org.eclipse.keyple.remote.example.model.TransactionResult;
import org.eclipse.keyple.remote.example.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example of a PluginObserver for a {@link RemotePluginServer}. It contains the business logic of
 * the remote service execution
 */
public class RemotePluginObserver implements ObservablePlugin.PluginObserver {

  /** AID: Keyple */
  public static final String AID = "315449432E49434131";

  public static final byte RECORD_NUMBER_1 = 1;
  public static final byte SFI_EnvironmentAndHolder = (byte) 0x07;
  public static final byte SFI_EventLog = (byte) 0x08;
  private static final Logger logger = LoggerFactory.getLogger(RemotePluginObserver.class);

  @Override
  public void update(PluginEvent event) {
    logger.info(
        "Event received {} {} {}",
        event.getEventType(),
        event.getPluginName(),
        event.getReaderNames().first());

    switch (event.getEventType()) {
      case READER_CONNECTED:
        // retrieve plugin
        RemotePluginServer plugin =
            (RemotePluginServer) SmartCardService.getInstance().getPlugin(event.getPluginName());

        // retrieve reader
        String remoteReaderName = event.getReaderNames().first();
        RemoteReaderServer remoteReader = plugin.getReader(remoteReaderName);

        // execute the business logic based on serviceId
        Object output = executeService(remoteReader);

        // terminate service
        plugin.terminateService(remoteReaderName, output);

        break;
    }
  }

  /**
   * Execute a service based on the serviceId of the virtual reader
   *
   * @param remoteReader the virtual reader on where to execute the business logic
   * @return output object
   */
  private Object executeService(RemoteReaderServer remoteReader) {

    /*
     * Retrieve the serviceId specified by the client when executing the remote service. Based on this serviceId, the server can select the ticketing logic to execute.
     */
    final String serviceId = remoteReader.getServiceId();
    logger.info("Executing ServiceId : {}", serviceId);

    // the service Id EXECUTE_CALYPSO_SESSION_FROM_REMOTE_SELECTION matches the following logic
    if ("EXECUTE_CALYPSO_SESSION_FROM_REMOTE_SELECTION".equals(serviceId)) {
      /*
       * Retrieve the userInputData specified by the client when executing the remote service.
       */
      UserInfo userInput = remoteReader.getUserInputData(UserInfo.class);

      /*
       * Execute an example of a ticketing transaction :
       * - perform a remote explicit selection
       * - read the content of event log file
       */
      // perform a remote explicit selection
      CardSelection seSelection = getSeSelection();
      SelectionsResult selectionsResult = seSelection.processExplicitSelection(remoteReader);
      CalypsoPo calypsoPo = (CalypsoPo) selectionsResult.getActiveSmartCard();

      try {
        // read the content of event log file
        readEventLog(calypsoPo, remoteReader);
        // return a successful transaction result
        return new TransactionResult().setUserId(userInput.getUserId()).setSuccessful(true);
      } catch (KeypleException e) {
        // if an exception is thrown, return an unsuccessful transaction result
        return new TransactionResult().setSuccessful(false).setUserId(userInput.getUserId());
      }
    }

    throw new IllegalArgumentException("Service Id not recognized");
  }

  /**
   * Prepare a Selection object ready to select Calypso card and read environment file
   *
   * @return instance of Selection object
   */
  private CardSelection getSeSelection() {
    // Prepare PO Selection
    CardSelection seSelection = new CardSelection();

    // Calypso selection
    PoSelectionRequest poSelectionRequest =
        new PoSelectionRequest(
            PoSelector.builder()
                .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                .aidSelector(CardSelector.AidSelector.builder().aidToSelect(AID).build())
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
   * @param reader native reader where the smartcard is inserted
   * @return content of the event log file in Hexadecimal
   */
  private String readEventLog(CalypsoPo calypsoPo, Reader reader) {
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

    PoTransaction poTransaction = new PoTransaction(new CardResource<>(reader, calypsoPo));

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
