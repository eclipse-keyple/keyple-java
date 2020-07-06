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
package org.eclipse.keyple.example.calypso.pc.usecase7;


import static org.eclipse.keyple.calypso.transaction.PoTransaction.SvSettings;
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
 * <h1>Use Case ‘Calypso 7’ – Stored Value (PC/SC)</h1><br>
 */
public class StoredValue_Pcsc {
    private static final Logger logger = LoggerFactory.getLogger(StoredValue_Pcsc.class);
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

        logger.info("=============== UseCase Calypso #7: Stored Value  ==================");
        logger.info("= PO Reader  NAME = {}", poReader.getName());
        logger.info("= SAM Reader  NAME = {}", samResource.getSeReader().getName());

        if (selectPo()) {
            // Security settings
            PoSecuritySettings poSecuritySettings =
                    new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)//
                            .build();

            // Create the PO resource
            SeResource<CalypsoPo> poResource;
            poResource = new SeResource<CalypsoPo>(poReader, calypsoPo);

            PoTransaction poTransaction = new PoTransaction(poResource, poSecuritySettings);

            poTransaction.processOpening(PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT);

            poTransaction.prepareSvGet(SvSettings.Operation.RELOAD, SvSettings.Action.DO);

            poTransaction.processPoCommandsInSession();

            logger.info("SV Get:");
            logger.info("Balance = {}", calypsoPo.getSvBalance());
            logger.info("Last Transaction Number = {}", calypsoPo.getSvLastTNum());
            logger.info("Load log record = {}", calypsoPo.getSvLoadLogRecord());
            logger.info("Debit log record = {}", calypsoPo.getSvDebitLogLastRecord());

            poTransaction.processClosing(ChannelControl.CLOSE_AFTER);
        } else {
            logger.error("The PO selection failed");
        }

        System.exit(0);
    }
}
