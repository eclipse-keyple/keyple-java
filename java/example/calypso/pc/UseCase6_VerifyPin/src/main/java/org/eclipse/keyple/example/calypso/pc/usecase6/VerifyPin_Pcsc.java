/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.example.calypso.pc.usecase6;


import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoPinException;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.CalypsoSam;
import org.eclipse.keyple.calypso.transaction.PoSecuritySettings;
import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.core.selection.SeResource;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.example.common.calypso.pc.transaction.CalypsoUtilities;
import org.eclipse.keyple.example.common.calypso.postructure.CalypsoClassicInfo;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>Use Case ‘Calypso 6’ – Verify Pin (outside and inside secure session) (PC/SC)</h1><br>
 * The example shows 4 successive presentations of PIN codes:
 * <ul>
 * <li>Outside session, transmission in plain
 * <li>Outside session, transmission encrypted
 * <li>Inside session, incorrect PIN
 * <li>Inside session, correct PIN
 * </ul>
 * The remaining attempt counter is logged after each operation.
 */
public class VerifyPin_Pcsc {
    private static final Logger logger = LoggerFactory.getLogger(VerifyPin_Pcsc.class);
    private static SeReader poReader;
    private static CalypsoPo calypsoPo;

    /**
     * Selects the PO
     *
     * @return true if the PO is selected
     * @throws KeypleReaderException in case of reader communication failure
     */
    private static boolean selectPo() {
        /* Check if a PO is present in the reader */
        if (poReader.isSePresent()) {
            logger.info(
                    "= ##### 1st PO exchange: AID based selection with reading of Environment file.");

            // Prepare a Calypso PO selection
            SeSelection seSelection = new SeSelection();

            // Setting of an AID based selection of a Calypso REV3 PO
            //
            // Select the first application matching the selection AID whatever the SE communication
            // protocol keep the logical channel open after the selection

            // Calypso selection: configures a PoSelectionRequest with all the desired attributes to
            // make the selection and read additional information afterwards
            PoSelectionRequest poSelectionRequest = new PoSelectionRequest(
                    PoSelector.builder().seProtocol(SeCommonProtocols.PROTOCOL_ISO14443_4)
                            .aidSelector(SeSelector.AidSelector.builder()
                                    .aidToSelect(CalypsoClassicInfo.AID).build())
                            .invalidatedPo(PoSelector.InvalidatedPo.REJECT).build());

            // Prepare the reading of the Environment and Holder file.
            poSelectionRequest.prepareReadRecordFile(CalypsoClassicInfo.SFI_EnvironmentAndHolder,
                    CalypsoClassicInfo.RECORD_NUMBER_1);

            // Add the selection case to the current selection
            //
            // (we could have added other cases here)
            seSelection.prepareSelection(poSelectionRequest);

            // Actual PO communication: operate through a single request the Calypso PO selection
            // and the file read
            calypsoPo = (CalypsoPo) seSelection.processExplicitSelection(poReader)
                    .getActiveMatchingSe();
            return true;
        } else {
            logger.error("No PO were detected.");
        }
        return false;
    }

    public static void main(String[] args) {

        // Get the instance of the SeProxyService (Singleton pattern)
        SeProxyService seProxyService = SeProxyService.getInstance();

        // Assign PcscPlugin to the SeProxyService
        seProxyService.registerPlugin(new PcscPluginFactory());

        // Get a PO reader ready to work with Calypso PO. Use the getReader helper method from the
        // CalypsoUtilities class.
        poReader = CalypsoUtilities.getDefaultPoReader();

        // Get a SAM reader ready to work with Calypso PO. Use the getReader helper method from the
        // CalypsoUtilities class.
        SeResource<CalypsoSam> samResource = CalypsoUtilities.getDefaultSamResource();

        logger.info("=============== UseCase Calypso #6: Verify PIN  ==================");
        logger.info("= PO Reader  NAME = {}", poReader.getName());
        logger.info("= SAM Reader  NAME = {}", samResource.getSeReader().getName());

        if (selectPo()) {
            // Security settings
            PoSecuritySettings poSecuritySettings =
                    new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)//
                            .pinCipheringKey((byte) 0x30, (byte) 0x79)//
                            .build();

            // Create the PO resource
            SeResource<CalypsoPo> poResource;
            poResource = new SeResource<CalypsoPo>(poReader, calypsoPo);
            String pinOk = "0000";
            String pinKo = "0001";

            // create an unsecured PoTransaction
            PoTransaction poTransactionUnsecured = new PoTransaction(poResource);

            ////////////////////////////
            // Verification of the PIN (correct) out of a secure session in plain mode
            poTransactionUnsecured.processVerifyPin(pinOk);
            logger.info("Remaining attempts #1: {}", calypsoPo.getPinAttemptRemaining());

            // create a secured PoTransaction
            PoTransaction poTransaction = new PoTransaction(poResource, poSecuritySettings);

            ////////////////////////////
            // Verification of the PIN (correct) out of a secure session in encrypted mode
            poTransaction.processVerifyPin(pinOk);
            logger.info("Remaining attempts #2: {}", calypsoPo.getPinAttemptRemaining());

            ////////////////////////////
            // Verification of the PIN (incorrect) inside a secure session
            poTransaction
                    .processOpening(PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT);
            try {
                poTransaction.processVerifyPin(pinKo);
            } catch (CalypsoPoPinException ex) {
                logger.error("PIN Exception: {}", ex.getMessage());
            }
            poTransaction.processCancel(ChannelControl.KEEP_OPEN);
            logger.error("Remaining attempts #3: {}", calypsoPo.getPinAttemptRemaining());

            ////////////////////////////
            // Verification of the PIN (correct) inside a secure session
            poTransaction
                    .processOpening(PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT);
            poTransaction.processVerifyPin(pinOk);
            poTransaction.processClosing(ChannelControl.CLOSE_AFTER);
            logger.info("Remaining attempts #4: {}", calypsoPo.getPinAttemptRemaining());
        } else {
            logger.error("The PO selection failed");
        }

        System.exit(0);
    }
}
