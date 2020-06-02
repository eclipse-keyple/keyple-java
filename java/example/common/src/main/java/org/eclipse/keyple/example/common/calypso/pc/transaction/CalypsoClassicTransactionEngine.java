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
package org.eclipse.keyple.example.common.calypso.pc.transaction;


import java.util.Map;
import java.util.SortedMap;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoCommandException;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamCommandException;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.ElementaryFile;
import org.eclipse.keyple.calypso.transaction.PoResource;
import org.eclipse.keyple.calypso.transaction.PoSecuritySettings;
import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.calypso.transaction.SamResource;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoTransactionException;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsResponse;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.common.calypso.postructure.CalypsoClassicInfo;
import org.eclipse.keyple.example.common.generic.AbstractReaderObserverEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;

/**
 * This Calypso demonstration code consists in:
 *
 * <ol>
 * <li>Setting up a two-reader configuration and adding an observer method ({@link #update update})
 * <li>Starting a card operation when a PO presence is notified (processSeMatch
 * operateSeTransaction)
 * <li>Opening a logical channel with the SAM (C1 SAM is expected) see
 * ({@link CalypsoClassicInfo#SAM_C1_ATR_REGEX SAM_C1_ATR_REGEX})
 * <li>Attempting to open a logical channel with the PO with 3 options:
 * <ul>
 * <li>Selecting with a fake AID (1)
 * <li>Selecting with the Calypso AID and reading the event log file
 * <li>Selecting with a fake AID (2)
 * </ul>
 * <li>Display {@link AbstractDefaultSelectionsResponse} data
 * <li>If the Calypso selection succeeded, do a Calypso transaction
 * ({doCalypsoReadWriteTransaction(PoTransaction, ApduResponse, boolean)}
 * doCalypsoReadWriteTransaction}).
 * </ol>
 *
 * <p>
 * The Calypso transactions demonstrated here shows the Keyple API in use with Calypso SE (PO and
 * SAM).
 *
 * <p>
 * Read the doc of each methods for further details.
 */
@SuppressWarnings("unused")
public class CalypsoClassicTransactionEngine extends AbstractReaderObserverEngine {
    private static Logger logger = LoggerFactory.getLogger(CalypsoClassicTransactionEngine.class);

    private SeReader poReader;
    private SeReader samReader;
    private SamResource samResource = null;

    private SeSelection seSelection;

    private boolean samChannelOpen;

    /* Constructor */
    public CalypsoClassicTransactionEngine() {
        super();
        this.samChannelOpen = false;
    }

    /* Assign readers to the transaction engine */
    public void setReaders(SeReader poReader, SeReader samReader) {
        this.poReader = poReader;
        this.samReader = samReader;
    }

    /**
     * Do a Calypso transaction
     * <p>
     * Nominal case (the previous transaction was ratified):
     * <ul>
     * <li>Process opening
     * <ul>
     * <li>Reading the event log file</li>
     * <li>Reading the contract list</li>
     * </ul>
     * </li>
     * <li>Process PO commands
     * <ul>
     * <li>Reading the 4 contracts</li>
     * </ul>
     * </li>
     * <li>Process closing
     * <ul>
     * <li>A new record is appended to the event log file</li>
     * <li>The session is closed in CONTACTLESS_MODE (a ratification command is sent)</li>
     * </ul>
     * </li>
     * </ul>
     * <p>
     * Alternate case (the previous transaction was not ratified):
     * <ul>
     * <li>Process opening
     * <ul>
     * <li>Reading the event log file</li>
     * <li>Reading the contract list</li>
     * </ul>
     * </li>
     * <li>Process closing
     * <ul>
     * <li>The session is closed in CONTACTLESS_MODE (a ratification command is sent)</li>
     * </ul>
     * </li>
     * </ul>
     * <p>
     * The PO logical channel is kept open or closed according to the closeSeChannel flag
     *
     *
     * @param calypsoPo the current {@link CalypsoPo}
     * @param poTransaction PoTransaction object
     * @param closeSeChannel flag to ask or not the channel closing at the end of the transaction
     * @throws CalypsoPoTransactionException if a functional error occurs (including PO and SAM IO
     *         errors)
     * @throws CalypsoPoCommandException if a PO command failed
     * @throws CalypsoSamCommandException if a SAM command failed
     */
    public void doCalypsoReadWriteTransaction(CalypsoPo calypsoPo, PoTransaction poTransaction,
            boolean closeSeChannel) throws CalypsoPoTransactionException, CalypsoPoCommandException,
            CalypsoSamCommandException {

        /*
         * Read commands to execute during the opening step: EventLog, ContractList
         */
        /* prepare Environment and Holder read record */
        poTransaction.prepareReadRecordFile(CalypsoClassicInfo.SFI_EnvironmentAndHolder,
                CalypsoClassicInfo.RECORD_NUMBER_1);

        /* prepare Event Log read record */
        poTransaction.prepareReadRecordFile(CalypsoClassicInfo.SFI_EventLog,
                CalypsoClassicInfo.RECORD_NUMBER_1);

        /* prepare Contract List read record */
        poTransaction.prepareReadRecordFile(CalypsoClassicInfo.SFI_ContractList,
                CalypsoClassicInfo.RECORD_NUMBER_1);

        if (logger.isInfoEnabled()) {
            logger.info(
                    "========= PO Calypso session ======= Opening ============================");
        }

        /*
         * Open Session for the debit key - with reading of the first record of the cyclic EF of
         * Environment and Holder file
         */
        poTransaction.processOpening(PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT);

        ElementaryFile efEventLog = calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_EventLog);
        byte[] eventLog = efEventLog.getData().getContent();
        logger.info("EventLog file: {}", ByteArrayUtil.toHex(eventLog));

        ElementaryFile efContractList = calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_ContractList);

        byte contractList[] = efContractList.getData().getContent(1);
        logger.info("ContractList file: {}", ByteArrayUtil.toHex(contractList));

        if (!calypsoPo.isDfRatified()) {
            logger.info(
                    "========= Previous Secure Session was not ratified. =====================");

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

            if (logger.isInfoEnabled()) {
                logger.info(
                        "========= PO Calypso session ======= Closing ============================");
            }

            /*
             * A ratification command will be sent (CONTACTLESS_MODE).
             */
            poTransaction.processClosing(ChannelControl.CLOSE_AFTER);

        } else {
            /*
             * [------------------------------------]
             *
             * Place to analyze the PO profile available in seResponse: Environment/Holder,
             * EventLog, ContractList.
             *
             * The information available allows the determination of the contract to be read.
             *
             * [------------------------------------]
             */

            if (logger.isInfoEnabled()) {
                logger.info(
                        "========= PO Calypso session ======= Processing of PO commands =======================");
            }

            /* Read all 4 contracts command, record size set to 29 */
            poTransaction.prepareReadRecordFile(CalypsoClassicInfo.SFI_Contracts,
                    CalypsoClassicInfo.RECORD_NUMBER_1, 4, 29);
            /* proceed with the sending of commands, don't close the channel */
            poTransaction.processPoCommandsInSession();

            ElementaryFile efContracts = calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_Contracts);

            SortedMap<Integer, byte[]> records = efContracts.getData().getAllRecordsContent();
            for (Map.Entry<Integer, byte[]> entry : records.entrySet()) {
                logger.info("Contract #{}: {}", entry.getKey(),
                        ByteArrayUtil.toHex(entry.getValue()));
            }

            if (logger.isInfoEnabled()) {
                logger.info(
                        "========= PO Calypso session ======= Closing ============================");
            }

            /*
             * [------------------------------------]
             *
             * Place to analyze the Contract (in seResponse).
             *
             * The content of the contract will grant or not access.
             *
             * In any case, a new record will be added to the EventLog.
             *
             * [------------------------------------]
             */

            /* prepare Event Log append record */


            poTransaction.prepareAppendRecord(CalypsoClassicInfo.SFI_EventLog,
                    ByteArrayUtil.fromHex(CalypsoClassicInfo.eventLog_dataFill));
            /*
             * A ratification command will be sent (CONTACTLESS_MODE).
             */
            poTransaction.processClosing(ChannelControl.CLOSE_AFTER);
        }

        logger.info("========= PO Calypso session ======= SUCCESS !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    public AbstractDefaultSelectionsRequest preparePoSelection() {
        /*
         * Initialize the selection process
         */
        seSelection = new SeSelection();

        /* operate multiple PO selections */
        String poFakeAid1 = "AABBCCDDEE"; // fake AID 1
        String poFakeAid2 = "EEDDCCBBAA"; // fake AID 2

        /*
         * Add selection case 1: Fake AID1, protocol ISO, target rev 3
         */
        seSelection.prepareSelection(new PoSelectionRequest(new PoSelector.Builder()
                .seProtocol(SeCommonProtocols.PROTOCOL_ISO14443_4)
                .aidSelector(new SeSelector.AidSelector.Builder().aidToSelect(poFakeAid1).build())
                .invalidatedPo(PoSelector.InvalidatedPo.REJECT).build()));

        /*
         * Add selection case 2: Calypso application, protocol ISO, target rev 2 or 3
         *
         * addition of read commands to execute following the selection
         */
        PoSelectionRequest poSelectionRequestCalypsoAid = new PoSelectionRequest(
                new PoSelector.Builder().seProtocol(SeCommonProtocols.PROTOCOL_ISO14443_4)
                        .aidSelector(new SeSelector.AidSelector.Builder()
                                .aidToSelect(CalypsoClassicInfo.AID).build())
                        .invalidatedPo(PoSelector.InvalidatedPo.ACCEPT).build());

        poSelectionRequestCalypsoAid.prepareSelectFile(CalypsoClassicInfo.LID_DF_RT);

        poSelectionRequestCalypsoAid.prepareSelectFile(CalypsoClassicInfo.LID_EventLog);

        poSelectionRequestCalypsoAid.prepareReadRecordFile(CalypsoClassicInfo.SFI_EventLog,
                CalypsoClassicInfo.RECORD_NUMBER_1);

        seSelection.prepareSelection(poSelectionRequestCalypsoAid);

        /*
         * Add selection case 3: Fake AID2, unspecified protocol, target rev 2 or 3
         */
        seSelection.prepareSelection(new PoSelectionRequest(new PoSelector.Builder()
                .seProtocol(SeCommonProtocols.PROTOCOL_B_PRIME)
                .aidSelector(new SeSelector.AidSelector.Builder().aidToSelect(poFakeAid2).build())
                .invalidatedPo(PoSelector.InvalidatedPo.REJECT).build()));

        /*
         * Add selection case 4: ATR selection, rev 1 atrregex
         */
        seSelection.prepareSelection(new PoSelectionRequest(
                new PoSelector.Builder().seProtocol(SeCommonProtocols.PROTOCOL_B_PRIME)
                        .atrFilter(new PoSelector.AtrFilter(CalypsoClassicInfo.ATR_REV1_REGEX))
                        .invalidatedPo(PoSelector.InvalidatedPo.REJECT).build()));

        return seSelection.getSelectionOperation();
    }

    /**
     * Do the PO selection and possibly go on with Calypso transactions.
     */
    @Override
    public void processSeMatch(AbstractDefaultSelectionsResponse defaultSelectionsResponse)
            throws KeypleException {
        CalypsoPo calypsoPo = (CalypsoPo) seSelection
                .processDefaultSelection(defaultSelectionsResponse).getActiveMatchingSe();
        if (calypsoPo != null) {
            logger.info("DF RT header: {}", calypsoPo.getDirectoryHeader());

            ElementaryFile eventLogEF = calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_EventLog);

            logger.info("Event Log header: {}", eventLogEF.getHeader());

            byte[] eventLogBytes =
                    eventLogEF.getData().getContent(CalypsoClassicInfo.RECORD_NUMBER_1);

            String eventLog = ByteArrayUtil.toHex(eventLogBytes);

            logger.info("EventLog: {}", eventLog);

            try {
                /* first time: check SAM */
                if (!this.samChannelOpen) {
                    /* the following method will throw an exception if the SAM is not available. */
                    samResource = CalypsoUtilities.checkSamAndOpenChannel(samReader);
                    this.samChannelOpen = true;
                }

                Profiler profiler = new Profiler("Entire transaction");

                /* Time measurement */
                profiler.start("Initial selection");

                profiler.start("Calypso1");

                /* define the SAM parameters to provide when creating PoTransaction */
                PoSecuritySettings poSecuritySettings =
                        CalypsoUtilities.getSecuritySettings(samResource);

                PoTransaction poTransaction =
                        new PoTransaction(new PoResource(poReader, calypsoPo), poSecuritySettings);

                doCalypsoReadWriteTransaction(calypsoPo, poTransaction, true);

                profiler.stop();
                logger.warn(System.getProperty("line.separator") + "{}", profiler);
            } catch (CalypsoPoCommandException e) {
                logger.error("PO command {} failed with the status code 0x{}. {}", e.getCommand(),
                        Integer.toHexString(e.getStatusCode() & 0xFFFF).toUpperCase(),
                        e.getMessage());
            }

        }
    }

    @Override
    public void processSeInserted() {
        logger.error("Unexpected SE insertion event");
    }

    @Override
    public void processSeRemoved() {
        logger.error("SE removal event");
    }

    @Override
    public void processUnexpectedSeRemoval() {
        logger.error("Unexpected SE removal event");
    }
}
