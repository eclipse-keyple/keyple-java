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
package org.eclipse.keyple.example.calypso.pc;


import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.example.calypso.common.postructure.CalypsoClassicInfo;
import org.eclipse.keyple.example.calypso.common.stub.se.StubCalypsoClassic;
import org.eclipse.keyple.example.calypso.common.stub.se.StubSamCalypsoClassic;
import org.eclipse.keyple.example.calypso.pc.transaction.CalypsoUtilities;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubProtocolSetting;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.plugin.stub.StubSecureElement;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.transaction.MatchingSe;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>Use Case ‘Calypso 4’ – PO Authentication (Stub)</h1>
 * <ul>
 * <li>
 * <h2>Scenario:</h2>
 * <ul>
 * <li>Initialize two stub readers (PO and SAM), insert a stub PO and a stub SAM.</li>
 * <li>Check if a ISO 14443-4 SE is in the reader, select a Calypso PO, operate a simple Calypso PO
 * authentication (open and close a secure session performed with the debit key).
 * <p>
 * The SAM messages are handled transparently by the Calypso transaction API.</li>
 * <li><code>
 Explicit Selection
 </code> means that it is the terminal application which start the SE processing.</li>
 * <li>4 PO messages:
 * <ul>
 * <li>1 - SE message to explicitly select the application in the reader</li>
 * <li>2 - transaction SE message to operate the session opening and a PO read</li>
 * <li>3 - transaction SE message to operate the reading of a file</li>
 * <li>4 - transaction SE message to operate the closing opening</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 */
public class UseCase_Calypso4_PoAuthentication_Stub {
    private static final Logger logger =
            LoggerFactory.getLogger(UseCase_Calypso4_PoAuthentication_Stub.class);

    public static void main(String[] args)
            throws KeypleBaseException, NoStackTraceThrowable, InterruptedException {

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Get the instance of the Stub plugin */
        StubPlugin stubPlugin = StubPlugin.getInstance();

        /* Assign StubPlugin to the SeProxyService */
        seProxyService.addPlugin(stubPlugin);

        /* Plug the PO stub reader. */
        stubPlugin.plugStubReader("poReader", true);

        /* Plug the SAM stub reader. */
        stubPlugin.plugStubReader("samReader", true);

        /*
         * Get a PO and a SAM reader ready to work with a Calypso PO.
         */
        StubReader poReader = (StubReader) (stubPlugin.getReader("poReader"));
        StubReader samReader = (StubReader) (stubPlugin.getReader("samReader"));

        /* Check if the reader exists */
        if (poReader == null || samReader == null) {
            throw new IllegalStateException("Bad PO or SAM reader setup");
        }

        /* Create 'virtual' Calypso PO */
        StubSecureElement calypsoStubSe = new StubCalypsoClassic();

        logger.info("Insert stub PO.");
        poReader.insertSe(calypsoStubSe);

        /* Create 'virtual' Calypso SAM */
        StubSecureElement calypsoSamStubSe = new StubSamCalypsoClassic();

        logger.info("Insert stub SAM.");
        samReader.insertSe(calypsoSamStubSe);

        /*
         * Open logical channel for the SAM inserted in the reader
         *
         * (We expect the right is inserted)
         */
        CalypsoUtilities.checkSamAndOpenChannel(samReader);

        /* Check if the readers exists */
        if (poReader == null || samReader == null) {
            throw new IllegalStateException("Bad PO or SAM reader setup");
        }

        logger.info("=============== UseCase Calypso #4: Po Authentication ==================");
        logger.info("= PO Reader  NAME = {}", poReader.getName());
        logger.info("= SAM Reader  NAME = {}", samReader.getName());

        poReader.addSeProtocolSetting(
                new SeProtocolSetting(StubProtocolSetting.SETTING_PROTOCOL_ISO14443_4));

        /* Check if a PO is present in the reader */
        if (poReader.isSePresent()) {

            logger.info(
                    "==================================================================================");
            logger.info(
                    "= 1st PO exchange: AID based selection with reading of Environment file.         =");
            logger.info(
                    "==================================================================================");

            /*
             * Prepare a Calypso PO selection
             */
            SeSelection seSelection = new SeSelection(poReader);

            /*
             * Setting of an AID based selection of a Calypso REV3 PO
             *
             * Select the first application matching the selection AID whatever the SE communication
             * protocol keep the logical channel open after the selection
             */

            /*
             * Calypso selection: configures a PoSelectionRequest with all the desired attributes to
             * make the selection and read additional information afterwards
             */
            PoSelectionRequest poSelectionRequest = new PoSelectionRequest(new PoSelector(
                    new PoSelector.PoAidSelector(ByteArrayUtils.fromHex(CalypsoClassicInfo.AID),
                            PoSelector.InvalidatedPo.REJECT),
                    null, "AID: " + CalypsoClassicInfo.AID), ChannelState.KEEP_OPEN,
                    ContactlessProtocols.PROTOCOL_ISO14443_4);

            /*
             * Add the selection case to the current selection (we could have added other cases
             * here)
             */
            CalypsoPo calypsoPo = (CalypsoPo) seSelection.prepareSelection(poSelectionRequest);

            /*
             * Actual PO communication: operate through a single request the Calypso PO selection
             * and the file read
             */
            if (seSelection.processExplicitSelection()) {
                logger.info("The selection of the PO has succeeded.");

                MatchingSe selectedSe = seSelection.getSelectedSe();

                /* Go on with the reading of the first record of the EventLog file */
                logger.info(
                        "==================================================================================");
                logger.info(
                        "= 2nd PO exchange: open and close a secure session to perform authentication.    =");
                logger.info(
                        "==================================================================================");

                PoTransaction poTransaction = new PoTransaction(poReader, (CalypsoPo) selectedSe,
                        samReader, CalypsoUtilities.getSamSettings());

                /*
                 * Prepare the reading order and keep the associated parser for later use once the
                 * transaction has been processed.
                 */
                ReadRecordsRespPars readEventLogParser = poTransaction.prepareReadRecordsCmd(
                        CalypsoClassicInfo.SFI_EventLog, ReadDataStructure.SINGLE_RECORD_DATA,
                        CalypsoClassicInfo.RECORD_NUMBER_1,
                        String.format("EventLog (SFI=%02X, recnbr=%d))",
                                CalypsoClassicInfo.SFI_EventLog,
                                CalypsoClassicInfo.RECORD_NUMBER_1));


                /*
                 * Open Session for the debit key
                 */
                boolean poProcessStatus = poTransaction.processOpening(
                        PoTransaction.ModificationMode.ATOMIC,
                        PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT, (byte) 0, (byte) 0);

                if (!poProcessStatus) {
                    throw new IllegalStateException("processingOpening failure.");
                }

                if (!poTransaction.wasRatified()) {
                    logger.info(
                            "========= Previous Secure Session was not ratified. =====================");
                }
                /*
                 * Prepare the reading order and keep the associated parser for later use once the
                 * transaction has been processed.
                 */
                ReadRecordsRespPars readEventLogParserBis = poTransaction.prepareReadRecordsCmd(
                        CalypsoClassicInfo.SFI_EventLog, ReadDataStructure.SINGLE_RECORD_DATA,
                        CalypsoClassicInfo.RECORD_NUMBER_1,
                        String.format("EventLog (SFI=%02X, recnbr=%d))",
                                CalypsoClassicInfo.SFI_EventLog,
                                CalypsoClassicInfo.RECORD_NUMBER_1));

                poProcessStatus = poTransaction.processPoCommandsInSession();

                /*
                 * Retrieve the data read from the parser updated during the transaction process
                 */
                byte eventLog[] = (readEventLogParser.getRecords())
                        .get((int) CalypsoClassicInfo.RECORD_NUMBER_1);

                /* Log the result */
                logger.info("EventLog file data: {}", ByteArrayUtils.toHex(eventLog));

                if (!poProcessStatus) {
                    throw new IllegalStateException("processPoCommandsInSession failure.");
                }

                /*
                 * Close the Secure Session.
                 */
                if (logger.isInfoEnabled()) {
                    logger.info(
                            "========= PO Calypso session ======= Closing ============================");
                }

                /*
                 * A ratification command will be sent (CONTACTLESS_MODE).
                 */
                poProcessStatus = poTransaction.processClosing(TransmissionMode.CONTACTLESS,
                        ChannelState.CLOSE_AFTER);

                if (!poProcessStatus) {
                    throw new IllegalStateException("processClosing failure.");
                }

                logger.info(
                        "==================================================================================");
                logger.info(
                        "= End of the Calypso PO processing.                                              =");
                logger.info(
                        "==================================================================================");
            } else {
                logger.error("The selection of the PO has failed.");
            }
        } else {
            logger.error("No PO were detected.");
        }
        System.exit(0);
    }
}
