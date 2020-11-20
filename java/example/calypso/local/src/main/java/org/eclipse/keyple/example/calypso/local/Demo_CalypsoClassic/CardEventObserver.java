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
package org.eclipse.keyple.example.calypso.local.Demo_CalypsoClassic;

import static org.eclipse.keyple.calypso.command.sam.SamRevision.C1;
import static org.eclipse.keyple.example.calypso.local.Demo_CalypsoClassic.CardSelectionConfig.getPoCardSelection;

import java.util.Map;
import java.util.SortedMap;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoCommandException;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamCommandException;
import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoTransactionException;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.card.selection.CardSelection;
import org.eclipse.keyple.core.card.selection.SelectionsResult;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.AbstractDefaultSelectionsResponse;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.calypso.local.common.CalypsoClassicInfo;
import org.eclipse.keyple.example.calypso.local.common.CalypsoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;

/**
 * Definition of the ticketing logic within the {@link ObservableReader.ReaderObserver#update}
 * method
 */
class CardEventObserver implements ObservableReader.ReaderObserver {
  Logger logger = LoggerFactory.getLogger(CardSelectionConfig.class);

  /* reference to the sam reader and sam resource to operate the transaction */
  private Reader samReader;
  private CardResource<CalypsoSam> samResource = null;

  /* Assign sam reader to the transaction engine */
  public void setSamReader(Reader samReader) {
    this.samReader = samReader;
  }

  /**
   * Do a Calypso transaction
   *
   * <p>Nominal case (the previous transaction was ratified):
   *
   * <ul>
   *   <li>Process opening
   *       <ul>
   *         <li>Reading the event log file
   *         <li>Reading the contract list
   *       </ul>
   *   <li>Process PO commands
   *       <ul>
   *         <li>Reading the 4 contracts
   *       </ul>
   *   <li>Process closing
   *       <ul>
   *         <li>A new record is appended to the event log file
   *         <li>The session is closed in CONTACTLESS_MODE (a ratification command is sent)
   *       </ul>
   * </ul>
   *
   * <p>Alternate case (the previous transaction was not ratified):
   *
   * <ul>
   *   <li>Process opening
   *       <ul>
   *         <li>Reading the event log file
   *         <li>Reading the contract list
   *       </ul>
   *   <li>Process closing
   *       <ul>
   *         <li>The session is closed in CONTACTLESS_MODE (a ratification command is sent)
   *       </ul>
   * </ul>
   *
   * <p>The PO logical channel is kept open or closed according to the closeSeChannel flag
   *
   * @param calypsoPo the current {@link CalypsoPo}
   * @param poTransaction PoTransaction object
   * @param closeSeChannel flag to ask or not the channel closing at the end of the transaction
   * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
   *     errors)
   * @throws CalypsoPoCommandException if a PO command failed
   * @throws CalypsoSamCommandException if a SAM command failed
   */
  private void doCalypsoReadWriteTransaction(
      CalypsoPo calypsoPo, PoTransaction poTransaction, boolean closeSeChannel) {

    /*
     * Read commands to execute during the opening step: EventLog, ContractList
     */
    /* prepare Environment and Holder read record */
    poTransaction.prepareReadRecordFile(
        CalypsoClassicInfo.SFI_EnvironmentAndHolder, CalypsoClassicInfo.RECORD_NUMBER_1);

    /* prepare Event Log read record */
    poTransaction.prepareReadRecordFile(
        CalypsoClassicInfo.SFI_EventLog, CalypsoClassicInfo.RECORD_NUMBER_1);

    /* prepare Contract List read record */
    poTransaction.prepareReadRecordFile(
        CalypsoClassicInfo.SFI_ContractList, CalypsoClassicInfo.RECORD_NUMBER_1);

    logger.info("========= PO Calypso session ======= Opening ============================");

    /*
     * Open Session for the debit key - with reading of the first record of the cyclic EF of
     * Environment and Holder file
     */
    poTransaction.processOpening(PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT);

    ElementaryFile efEventLog = calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_EventLog);
    byte[] eventLog = efEventLog.getData().getContent();
    logger.info("EventLog file: {}", ByteArrayUtil.toHex(eventLog));

    ElementaryFile efContractList = calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_ContractList);

    byte[] contractList = efContractList.getData().getContent(1);
    logger.info("ContractList file: {}", ByteArrayUtil.toHex(contractList));

    if (!calypsoPo.isDfRatified()) {
      logger.info("========= Previous Secure Session was not ratified. =====================");

      /*
       * [------------------------------------]
       *
       * The previous Secure Session has not been ratified, so we simply close the Secure
       * Session.
       *
       * We would analyze here the event log read during the opening phase.
       *
       * [------------------------------------]
       */

      logger.info("========= PO Calypso session ======= Closing ============================");

      /*
       * A ratification command will be sent (CONTACTLESS_MODE).
       */
      poTransaction.prepareReleasePoChannel();
      poTransaction.processClosing();

    } else {
      /*
       * [------------------------------------]
       *
       * Place to analyze the PO profile available in cardResponse: Environment/Holder,
       * EventLog, ContractList.
       *
       * The information available allows the determination of the contract to be read.
       *
       * [------------------------------------]
       */

      logger.info(
          "========= PO Calypso session ======= Processing of PO commands =======================");

      /* Read all 4 contracts command, record size set to 29 */
      poTransaction.prepareReadRecordFile(
          CalypsoClassicInfo.SFI_Contracts, CalypsoClassicInfo.RECORD_NUMBER_1, 4, 29);
      /* proceed with the sending of commands, don't close the channel */
      poTransaction.processPoCommands();

      ElementaryFile efContracts = calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_Contracts);

      SortedMap<Integer, byte[]> records = efContracts.getData().getAllRecordsContent();
      for (Map.Entry<Integer, byte[]> entry : records.entrySet()) {
        logger.info("Contract #{}: {}", entry.getKey(), ByteArrayUtil.toHex(entry.getValue()));
      }

      logger.info("========= PO Calypso session ======= Closing ============================");

      /*
       * [------------------------------------]
       *
       * Place to analyze the Contract (in cardResponse).
       *
       * The content of the contract will grant or not access.
       *
       * In any case, a new record will be added to the EventLog.
       *
       * [------------------------------------]
       */

      /* prepare Event Log append record */

      poTransaction.prepareAppendRecord(
          CalypsoClassicInfo.SFI_EventLog,
          ByteArrayUtil.fromHex(CalypsoClassicInfo.eventLog_dataFill));
      /*
       * A ratification command will be sent (CONTACTLESS_MODE).
       */
      poTransaction.prepareReleasePoChannel();
      poTransaction.processClosing();
    }

    logger.info("========= PO Calypso session ======= SUCCESS !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
  }

  /** Do the PO selection and possibly go on with Calypso transactions. */
  private void operateCalypsoTransaction(
      AbstractDefaultSelectionsResponse defaultSelectionsResponse, Reader poReader) {
    CalypsoPo calypsoPo =
        (CalypsoPo)
            getPoCardSelection()
                .processDefaultSelection(defaultSelectionsResponse)
                .getActiveSmartCard();

    if (calypsoPo == null) {
      throw new IllegalStateException("PO selection has failed");
    }

    logger.info("DF RT header: {}", calypsoPo.getDirectoryHeader());

    ElementaryFile eventLogEF = calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_EventLog);

    logger.info("Event Log header: {}", eventLogEF.getHeader());

    byte[] eventLogBytes = eventLogEF.getData().getContent(CalypsoClassicInfo.RECORD_NUMBER_1);

    String eventLog = ByteArrayUtil.toHex(eventLogBytes);

    logger.info("EventLog: {}", eventLog);

    try {
      /* first time: create a SAM resource */
      if (samResource == null) {
        /* the following method will throw an exception if the SAM is not available. */
        samResource = getSamResource();
      }

      Profiler profiler = new Profiler("Entire transaction");

      /* Time measurement */
      profiler.start("Initial selection");

      profiler.start("Calypso1");

      /* define the SAM parameters to provide when creating PoTransaction */
      PoSecuritySettings poSecuritySettings = CalypsoUtils.getSecuritySettings(samResource);

      PoTransaction poTransaction =
          new PoTransaction(new CardResource<CalypsoPo>(poReader, calypsoPo), poSecuritySettings);

      doCalypsoReadWriteTransaction(calypsoPo, poTransaction, true);

      profiler.stop();
      logger.warn("{}{}", System.getProperty("line.separator"), profiler);
    } catch (CalypsoPoCommandException e) {
      logger.error(
          "PO command {} failed with the status code 0x{}. {}",
          e.getCommand(),
          Integer.toHexString(e.getStatusCode() & 0xFFFF).toUpperCase(),
          e.getMessage());
    }
  }

  @Override
  public void update(ReaderEvent event) {
    logger.info("New reader event: {}", event.getReaderName());

    switch (event.getEventType()) {
      case CARD_INSERTED:
        break;

      case CARD_MATCHED:
        Reader poReader =
            SmartCardService.getInstance()
                .getPlugin(event.getPluginName())
                .getReader(event.getReaderName());
        operateCalypsoTransaction(event.getDefaultSelectionsResponse(), poReader);
        break;

      case CARD_REMOVED:
        break;
      case UNREGISTERED:
        throw new IllegalStateException(
            "Unexpected error: the reader is no more registered in the SmartcardService.");
    }
  }

  private CardResource<CalypsoSam> getSamResource() {
    // Create a SAM resource after selecting the SAM
    CardSelection samSelection = new CardSelection();

    SamSelector samSelector = SamSelector.builder().samRevision(C1).serialNumber(".*").build();

    // Prepare selector
    samSelection.prepareSelection(new SamSelectionRequest(samSelector));
    CalypsoSam calypsoSam;
    if (!samReader.isCardPresent()) {
      throw new IllegalStateException("No SAM is present in the reader " + samReader.getName());
    }
    SelectionsResult selectionsResult = samSelection.processExplicitSelection(samReader);
    if (!selectionsResult.hasActiveSelection()) {
      throw new IllegalStateException("Unable to open a logical channel for SAM!");
    }
    calypsoSam = (CalypsoSam) selectionsResult.getActiveSmartCard();

    return new CardResource<CalypsoSam>(samReader, calypsoSam);
  }
}
