/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.example.calypso.pc.usecase1;



import static org.eclipse.keyple.calypso.transaction.PoSelector.*;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.ElementaryFile;
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
 * <h1>Use Case ‘Calypso 1’ – Explicit Selection Aid (PC/SC)</h1>
 * <ul>
 * <li>
 * <h2>Scenario:</h2>
 * <ul>
 * <li>Check if a ISO 14443-4 SE is in the reader, select a Calypso PO, operate a simple Calypso PO
 * transaction (simple plain read, not involving a Calypso SAM).</li>
 * <li><code>
 Explicit Selection
 </code> means that it is the terminal application which start the SE processing.</li>
 * <li>PO messages:
 * <ul>
 * <li>A first SE message to select the application in the reader</li>
 * <li>A second SE message to operate the simple Calypso transaction</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 */
public class ExplicitSelectionAid_Pcsc {
    private static final Logger logger = LoggerFactory.getLogger(ExplicitSelectionAid_Pcsc.class);

    public static void main(String[] args) {

        // Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        // Assign PcscPlugin to the SeProxyService
        seProxyService.registerPlugin(new PcscPluginFactory());

        // Get a PO reader ready to work with Calypso PO. Use the getReader helper method from the
        // CalypsoUtilities class.
        SeReader poReader = CalypsoUtilities.getDefaultPoReader();

        logger.info(
                "=============== UseCase Calypso #1: AID based explicit selection ==================");
        logger.info("= PO Reader  NAME = {}", poReader.getName());

        // Check if a PO is present in the reader
        if (poReader.isSePresent()) {

            logger.info(
                    "= #### 1st PO exchange: AID based selection with reading of Environment file.");

            // Prepare a Calypso PO selection
            SeSelection seSelection = new SeSelection();

            // Setting of an AID based selection of a Calypso REV3 PO
            // Select the first application matching the selection AID whatever the SE
            // communication
            // protocol keep the logical channel open after the selection

            // Calypso selection: configures a PoSelectionRequest with all the desired attributes to
            // make the selection and read additional information afterwards
            PoSelectionRequest poSelectionRequest = new PoSelectionRequest(PoSelector.builder()
                    .seProtocol(SeCommonProtocols.PROTOCOL_ISO14443_4)
                    .aidSelector(AidSelector.builder().aidToSelect(CalypsoClassicInfo.AID).build())
                    .invalidatedPo(InvalidatedPo.REJECT).build());

            // Prepare the reading order.
            poSelectionRequest.prepareReadRecordFile(CalypsoClassicInfo.SFI_EnvironmentAndHolder,
                    CalypsoClassicInfo.RECORD_NUMBER_1);

            // Add the selection case to the current selection (we could have added other cases
            // here)
            // Ignore the returned index since we have only one selection here.
            seSelection.prepareSelection(poSelectionRequest);

            // Actual PO communication: operate through a single request the Calypso PO selection
            // and the file read

            CalypsoPo calypsoPo = (CalypsoPo) seSelection.processExplicitSelection(poReader)
                    .getActiveMatchingSe();
            logger.info("The selection of the PO has succeeded.");

            // Retrieve the data read from the CalyspoPo updated during the transaction process
            ElementaryFile efEnvironmentAndHolder =
                    calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_EnvironmentAndHolder);
            String environmentAndHolder =
                    ByteArrayUtil.toHex(efEnvironmentAndHolder.getData().getContent());

            // Log the result
            logger.info("EnvironmentAndHolder file data: {}", environmentAndHolder);


            /* Go on with the reading of the first record of the EventLog file */
            logger.info("= #### 2nd PO exchange: reading transaction of the EventLog file.");

            PoTransaction poTransaction =
                    new PoTransaction(new SeResource<CalypsoPo>(poReader, calypsoPo));

            // Prepare the reading order.
            poTransaction.prepareReadRecordFile(CalypsoClassicInfo.SFI_EventLog,
                    CalypsoClassicInfo.RECORD_NUMBER_1);

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

            logger.info("= #### End of the Calypso PO processing.");
        } else {
            logger.error("No PO were detected.");
        }
        System.exit(0);
    }
}
