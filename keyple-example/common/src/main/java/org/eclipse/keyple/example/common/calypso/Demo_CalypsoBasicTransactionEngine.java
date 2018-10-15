/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.example.common.calypso;

import static org.eclipse.keyple.example.common.calypso.CalypsoBasicInfo.*;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import org.eclipse.keyple.calypso.transaction.CalypsoPO;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.example.common.generic.DemoHelpers;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeResponse;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;

/**
 * This Calypso demonstration code consists in:
 *
 * <ol>
 * <li>Setting up a two-reader configuration and adding an observer method ({@link #update update})
 * <li>Starting a card operation when a PO presence is notified ({@link #operatePoTransactions
 * operatePoTransactions})
 * <li>Opening a logical channel with the CSM (C1 CSM is expected) see
 * ({@link CalypsoBasicInfo#CSM_C1_ATR_REGEX CSM_C1_ATR_REGEX})
 * <li>Attempting to open a logical channel with the PO with 3 options:
 * <ul>
 * <li>Selection with a fake AID (1)
 * <li>Selection with a Navigo AID
 * <li>Selection with a fake AID (2)
 * </ul>
 * <li>Display SeResponse data
 * <li>If the Calypso selection succeeded, do a Calypso transaction
 * ({doCalypsoReadWriteTransaction(PoTransaction, ApduResponse, boolean)}
 * doCalypsoReadWriteTransaction}).
 * </ol>
 *
 * <p>
 * The Calypso transactions demonstrated here shows the Keyple API in use with Calypso SE (PO and
 * CSM).
 *
 * <p>
 * Read the doc of each methods for further details.
 */
public class Demo_CalypsoBasicTransactionEngine extends DemoHelpers
        implements ObservableReader.ReaderObserver {
    private static Logger logger =
            LoggerFactory.getLogger(Demo_CalypsoBasicTransactionEngine.class);

    /* define the CSM parameters to provide when creating PoTransaction */
    private final static EnumMap<PoTransaction.CsmSettings, Byte> csmSetting =
            new EnumMap<PoTransaction.CsmSettings, Byte>(PoTransaction.CsmSettings.class) {
                {
                    put(PoTransaction.CsmSettings.CS_DEFAULT_KIF_PERSO,
                            PoTransaction.DEFAULT_KIF_PERSO);
                    put(PoTransaction.CsmSettings.CS_DEFAULT_KIF_LOAD,
                            PoTransaction.DEFAULT_KIF_LOAD);
                    put(PoTransaction.CsmSettings.CS_DEFAULT_KIF_DEBIT,
                            PoTransaction.DEFAULT_KIF_DEBIT);
                    put(PoTransaction.CsmSettings.CS_DEFAULT_KEY_RECORD_NUMBER,
                            PoTransaction.DEFAULT_KEY_RECORD_NUMER);
                }
            };

    private ProxyReader poReader, csmReader;

    private boolean csmChannelOpen;

    /* Constructor */
    public Demo_CalypsoBasicTransactionEngine() {
        super();
        this.csmChannelOpen = false;
    }

    /* Assign readers to the transaction engine */
    public void setReaders(ProxyReader poReader, ProxyReader csmReader) {
        this.poReader = poReader;
        this.csmReader = csmReader;
    }

    /**
     * Do an Calypso transaction:
     * <ul>
     * <li>Process opening</li>
     * <li>Process PO commands</li>
     * <li>Process closing</li>
     * </ul>
     * <p>
     * File with SFI 1A is read on session opening.
     * <p>
     * T2 Environment and T2 Usage are read in session.
     * <p>
     * The PO logical channel is kept open or closed according to the closeSeChannel flag
     *
     * @param poTransaction PoTransaction object
     * @param closeSeChannel flag to ask or not the channel closing at the end of the transaction
     * @throws KeypleReaderException reader exception (defined as public for purposes of javadoc)
     */
    public void doCalypsoReadWriteTransaction(PoTransaction poTransaction, boolean closeSeChannel)
            throws KeypleReaderException {

        /* SeResponse object to receive the results of PoTransaction operations. */
        SeResponse seResponse;

        /*
         * Read commands to execute during the opening step: EventLog, ContractList
         */

        /* prepare Event Log read record */
        poTransaction.prepareReadRecordsCmd(SFI_EventLog, RECORD_NUMBER_1, true, (byte) 0x00,
                String.format("EventLog (SFI=%02X, recnbr=%d))", SFI_EventLog, RECORD_NUMBER_1));


        /* prepare Contract List read record */
        poTransaction.prepareReadRecordsCmd(SFI_ContractList, RECORD_NUMBER_1, true, (byte) 0x00,
                String.format("ContractList (SFI=%02X))", SFI_ContractList));

        if (logger.isInfoEnabled()) {
            logger.info(
                    "========= PO Calypso session ======= Opening ============================");
        }

        /*
         * Open Session for the debit key - with reading of the first record of the cyclic EF of
         * Environment and Holder file
         */
        seResponse = poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT, SFI_EnvironmentAndHolder,
                RECORD_NUMBER_1);

        if (!poTransaction.wasRatified()) {
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
            seResponse = poTransaction
                    .processClosing(PoTransaction.CommunicationMode.CONTACTLESS_MODE, false);

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

            /* Read contract command (we assume we have determine Contract #1 to be read. */
            /* prepare Contract #1 read record */
            poTransaction.prepareReadRecordsCmd(SFI_Contracts, RECORD_NUMBER_1, true, (byte) 0x00,
                    String.format("Contracts (SFI=%02X, recnbr=%d)", SFI_Contracts,
                            RECORD_NUMBER_1));

            seResponse = poTransaction.processPoCommands();

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
            poTransaction.prepareAppendRecordCmd(SFI_EventLog,
                    ByteArrayUtils.fromHex(eventLog_dataFill),
                    String.format("EventLog (SFI=%02X)", SFI_EventLog));

            /*
             * A ratification command will be sent (CONTACTLESS_MODE).
             */
            seResponse = poTransaction
                    .processClosing(PoTransaction.CommunicationMode.CONTACTLESS_MODE, false);
        }

        if (poTransaction.isSuccessful()) {
            if (logger.isInfoEnabled()) {
                logger.info(
                        "========= PO Calypso session ======= SUCCESS !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        } else {
            logger.error(
                    "========= PO Calypso session ======= ERROR !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    /**
     * Do the PO selection and possibly go on with Calypso transactions.
     */
    public void operatePoTransactions() {
        try {
            /* first time: check CSM */
            if (!this.csmChannelOpen) {
                /* the following method will throw an exception if the CSM is not available. */
                checkCsmAndOpenChannel(csmReader);
                this.csmChannelOpen = true;
            }

            Profiler profiler = new Profiler("Entire transaction");

            /* operate multiple PO selections */
            String poFakeAid1 = "AABBCCDDEE"; // fake AID 1
            String poFakeAid2 = "EEDDCCBBAA"; // fake AID 2

            /*
             * Prepare the selection using the SeSelection class
             */
            SeSelection seSelection = new SeSelection(poReader);

            /*
             * Add selection case 1: Fake AID1, protocol ISO, target rev 3
             */

            seSelection.addSelector(new PoSelector(ByteArrayUtils.fromHex(poFakeAid1), true,
                    ContactlessProtocols.PROTOCOL_ISO14443_4,
                    PoSelector.RevisionTarget.TARGET_REV3));

            /*
             * Add selection case 2: Calypso application, protocol ISO, target rev 2 or 3
             *
             * addition of read commands to execute following the selection
             */
            PoSelector poSelectorCalypsoAid =
                    new PoSelector(ByteArrayUtils.fromHex(CalypsoBasicInfo.AID), true,
                            ContactlessProtocols.PROTOCOL_ISO14443_4,
                            PoSelector.RevisionTarget.TARGET_REV2_REV3);

            poSelectorCalypsoAid.prepareReadRecordsCmd(SFI_EventLog, RECORD_NUMBER_1, true,
                    (byte) 0x00, "EventLog (selection step)");

            seSelection.addSelector(poSelectorCalypsoAid);

            /*
             * Add selection case 3: Fake AID2, unspecified protocol, target rev 2 or 3
             */

            seSelection.addSelector(new PoSelector(ByteArrayUtils.fromHex(poFakeAid2), true,
                    ContactlessProtocols.PROTOCOL_ISO14443_4,
                    PoSelector.RevisionTarget.TARGET_REV2_REV3));

            /* Time measurement */
            profiler.start("Initial selection");

            /*
             * Execute the selection process
             */
            List<SeResponse> seResponses = seSelection.processSelection().getResponses();

            int responseIndex = 0;

            /*
             * we expect up to 3 responses, only one should be not null since the selection process
             * stops at the first successful selection
             *
             * TODO improve the response analysis
             */
            if (logger.isInfoEnabled()) {
                for (Iterator<SeResponse> seRespIterator = seResponses.iterator(); seRespIterator
                        .hasNext();) {
                    SeResponse seResponse = seRespIterator.next();
                    if (seResponse != null) {
                        logger.info("Selection case #{}, RESPONSE = {}", responseIndex,
                                seResponse.getApduResponses());
                    }
                    responseIndex++;
                }
            }

            /*
             * If the Calypso selection succeeded we should have 2 responses and the 2nd one not
             * null
             */
            if (seResponses.size() == 2 && seResponses.get(1) != null) {

                profiler.start("Calypso1");

                PoTransaction poTransaction = new PoTransaction(poReader,
                        new CalypsoPO(seResponses.get(1)), csmReader, csmSetting);

                doCalypsoReadWriteTransaction(poTransaction, true);

            } else {
                logger.error("No Calypso transaction. SeResponse to Calypso selection was null.");
            }

            profiler.stop();
            logger.warn(System.getProperty("line.separator") + "{}", profiler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
