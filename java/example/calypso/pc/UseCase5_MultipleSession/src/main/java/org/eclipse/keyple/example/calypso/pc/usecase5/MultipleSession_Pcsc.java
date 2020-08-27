/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.example.calypso.pc.usecase5;

import static org.eclipse.keyple.calypso.transaction.PoSelector.*;

import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.CalypsoSam;
import org.eclipse.keyple.calypso.transaction.PoSecuritySettings;
import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.core.selection.SeResource;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.common.calypso.pc.transaction.CalypsoUtilities;
import org.eclipse.keyple.example.common.calypso.postructure.CalypsoClassicInfo;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * <h1>Use Case ‘Calypso 5’ – PO Multiple Sessions (PC/SC)</h1>
 *
 * <ul>
 *   <li>
 *       <h2>Scenario:</h2>
 *       <ul>
 *         <li>Check if a ISO 14443-4 SE is in the reader, select a Calypso PO, operate a Calypso PO
 *             transaction in multiple mode including a number (N) of modification commands that
 *             exceed by one command the PO modification buffer. (open and close a secure session
 *             performed with the debit key).
 *             <p>Two sessions are performed:
 *             <ul>
 *               <li>A first session proceeds the N-1 first modification commands
 *               <li>A second session proceeds the last modification command
 *             </ul>
 *             <p>The SAM messages are handled transparently by the Calypso transaction API.
 *         <li>
 *             <p><code>
 * Explicit Selection
 * </code> means that it is the terminal application which start the SE processing.
 *         <li>PO messages:
 *             <ul>
 *               <li>1 - SE message to explicitly select the application in the reader
 *               <li>2 - transaction SE message to operate the session opening in multiple mode
 *               <li>3 - transaction SE message to operate multiple updates of the same file (a
 *                   first session proceeding with the first modification commands is open and
 *                   closed)
 *               <li>4 - transaction SE message to operate the closing opening
 *             </ul>
 *       </ul>
 * </ul>
 */
public class MultipleSession_Pcsc {
  private static final Logger logger = LoggerFactory.getLogger(MultipleSession_Pcsc.class);

  public static void main(String[] args) {

    // Get the instance of the SeProxyService (Singleton pattern)
    SeProxyService seProxyService = SeProxyService.getInstance();

    // Assign PcscPlugin to the SeProxyService
    seProxyService.registerPlugin(new PcscPluginFactory());

    // Get a PO reader ready to work with Calypso PO. Use the getReader helper method from the
    // CalypsoUtilities class.
    SeReader poReader = CalypsoUtilities.getDefaultPoReader();

    // Get a SAM reader ready to work with Calypso PO. Use the getReader helper method from
    // the
    // CalypsoUtilities class.
    SeResource<CalypsoSam> samResource = CalypsoUtilities.getDefaultSamResource();

    logger.info("=============== UseCase Calypso #5: Po Authentication ==================");
    logger.info("= PO Reader  NAME = {}", poReader.getName());
    logger.info("= SAM Reader  NAME = {}", samResource.getSeReader().getName());

    // Check if a PO is present in the reader
    if (poReader.isSePresent()) {

      logger.info("= #### 1st PO exchange: AID based selection with reading of Environment file.");

      // Prepare a Calypso PO selection
      SeSelection seSelection = new SeSelection();

      // Setting of an AID based selection of a Calypso REV3 PO
      //
      // Select the first application matching the selection AID whatever the SE
      // communication
      // protocol keep the logical channel open after the selection

      // Calypso selection: configures a PoSelectionRequest with all the desired attributes
      // to
      // make the selection and read additional information afterwards
      PoSelectionRequest poSelectionRequest =
          new PoSelectionRequest(
              PoSelector.builder()
                  .seProtocol(SeCommonProtocols.PROTOCOL_ISO14443_4)
                  .aidSelector(AidSelector.builder().aidToSelect(CalypsoClassicInfo.AID).build())
                  .invalidatedPo(InvalidatedPo.REJECT)
                  .build());

      // Add the selection case to the current selection (we could have added other cases
      // here)
      seSelection.prepareSelection(poSelectionRequest);

      // Actual PO communication: operate through a single request the Calypso PO selection
      // and the file read
      CalypsoPo calypsoPo =
          (CalypsoPo) seSelection.processExplicitSelection(poReader).getActiveMatchingSe();
      logger.info("The selection of the PO has succeeded.");

      // Go on with the reading of the first record of the EventLog file
      logger.info(
          "= #### 2nd PO exchange: open and close a secure session to perform authentication.");

      // The default KIF values for debiting
      final byte DEFAULT_KIF_DEBIT = (byte) 0x30;
      // The default key record number values for debiting
      // The actual value should be adjusted.
      final byte DEFAULT_KEY_RECORD_NUMBER_DEBIT = (byte) 0x03;
      // define the security parameters to provide when creating PoTransaction
      PoSecuritySettings poSecuritySettings =
          new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)
              .sessionDefaultKif(
                  PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT)
              .sessionDefaultKeyRecordNumber(
                  PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT,
                  DEFAULT_KEY_RECORD_NUMBER_DEBIT)
              .sessionModificationMode(PoTransaction.SessionSetting.ModificationMode.MULTIPLE)
              .build();

      PoTransaction poTransaction =
          new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPo), poSecuritySettings);

      // Open Session for the debit key
      poTransaction.processOpening(PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT);

      if (!calypsoPo.isDfRatified()) {
        logger.info("========= Previous Secure Session was not ratified. =====================");
      }
      // Compute the number of append records (29 bytes) commands that will overflow the PO
      // modifications buffer. Each append records will consume 35 (29 + 6) bytes in the
      // buffer.
      //
      // We'll send one more command to demonstrate the MULTIPLE mode
      int modificationsBufferSize = 430; // not all PO have this buffer size

      int nbCommands = (modificationsBufferSize / 35) + 1;

      logger.info(
          "==== Send {} Append Record commands. Modifications buffer capacity = {} bytes i.e. {} 29-byte commands ====",
          nbCommands,
          modificationsBufferSize,
          modificationsBufferSize / 35);

      for (int i = 0; i < nbCommands; i++) {

        poTransaction.prepareAppendRecord(
            CalypsoClassicInfo.SFI_EventLog,
            ByteArrayUtil.fromHex(CalypsoClassicInfo.eventLog_dataFill));
      }

      // proceed with the sending of commands, don't close the channel
      poTransaction.processPoCommands();

      // Close the Secure Session.

      logger.info("========= PO Calypso session ======= Closing ============================");

      // A ratification command will be sent (CONTACTLESS_MODE).
      poTransaction.processClosing();

      logger.info("= #### End of the Calypso PO processing.");
    } else {
      logger.error("The selection of the PO has failed.");
    }
    System.exit(0);
  }
}
