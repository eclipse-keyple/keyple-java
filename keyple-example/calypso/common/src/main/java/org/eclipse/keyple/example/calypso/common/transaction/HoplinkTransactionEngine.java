/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */
package org.eclipse.keyple.example.calypso.common.transaction;

import java.util.*;
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.example.calypso.common.postructure.HoplinkInfo;
import org.eclipse.keyple.example.generic.common.AbstractReaderObserverEngine;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.transaction.MatchingSe;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.transaction.SeSelector;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;

/**
 * This Calypso demonstration code consists in:
 *
 * <ol>
 * <li>Setting up a two-reader configuration and adding an observer method ({@link #update update})
 * <li>Starting a card operation when a PO presence is notified ({@link #operateSeTransaction
 * operateSeTransaction})
 * <li>Opening a logical channel with the SAM (C1 SAM is expected) see
 * ({@link HoplinkInfo#SAM_C1_ATR_REGEX SAM_C1_ATR_REGEX})
 * <li>Attempting to open a logical channel with the PO with 3 options:
 * <ul>
 * <li>Selection with a fake AID
 * <li>Selection with a Navigo AID
 * <li>Selection with a Hoplink AID
 * </ul>
 * <li>If the Hoplink selection succeeded, do an Hoplink transaction
 * ({doHoplinkReadWriteTransaction(PoTransaction, ApduResponse, boolean)}
 * doHoplinkReadWriteTransaction}).
 * </ol>
 *
 * <p>
 * The Hoplink transactions demonstrated here shows the Keyple API in use with Hoplink SE (PO and
 * SAM).
 *
 * <p>
 * Read the doc of each methods for further details.
 */
public class HoplinkTransactionEngine extends AbstractReaderObserverEngine {
    private final static Logger logger = LoggerFactory.getLogger(HoplinkTransactionEngine.class);

    /* define the SAM parameters to provide when creating PoTransaction */
    private final static EnumMap<PoTransaction.SamSettings, Byte> samSetting =
            new EnumMap<PoTransaction.SamSettings, Byte>(PoTransaction.SamSettings.class) {
                {
                    put(PoTransaction.SamSettings.SAM_DEFAULT_KIF_PERSO,
                            PoTransaction.DEFAULT_KIF_PERSO);
                    put(PoTransaction.SamSettings.SAM_DEFAULT_KIF_LOAD,
                            PoTransaction.DEFAULT_KIF_LOAD);
                    put(PoTransaction.SamSettings.SAM_DEFAULT_KIF_DEBIT,
                            PoTransaction.DEFAULT_KIF_DEBIT);
                    put(PoTransaction.SamSettings.SAM_DEFAULT_KEY_RECORD_NUMBER,
                            PoTransaction.DEFAULT_KEY_RECORD_NUMER);
                }
            };

    private ProxyReader poReader;
    private ProxyReader samReader;

    private SeSelection seSelection;

    private Profiler profiler;

    private boolean samChannelOpen;

    /* Constructor */
    public HoplinkTransactionEngine() {
        super();
        this.samChannelOpen = false;
    }

    /* Assign readers to the transaction engine */
    public void setReaders(ProxyReader poReader, ProxyReader samReader) {
        this.poReader = poReader;
        this.samReader = samReader;
    }

    /**
     * Do a Hoplink transaction:
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
    public void doHoplinkReadWriteTransaction(PoTransaction poTransaction, boolean closeSeChannel)
            throws KeypleReaderException {
        boolean poProcessStatus;

        /*
         * the modification command sent sent on closing is disabled for the moment due to CAAD
         * configuration of the current Hoplink test PO
         */
        if (logger.isInfoEnabled()) {
            logger.info(
                    "========= PO Hoplink session ======= Opening ============================");
        }

        /*
         * Open Session for the debit key - with reading of the first record of the T2 Environment
         * file (read twice in this demo!)
         */
        poProcessStatus = poTransaction.processOpening(PoTransaction.ModificationMode.ATOMIC,
                PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT, HoplinkInfo.SFI_T2Environment,
                HoplinkInfo.RECORD_NUMBER_1);

        if (!poTransaction.wasRatified()) {
            logger.info("### Previous Secure Session was not ratified. ###");
        }

        if (logger.isInfoEnabled()) {
            logger.info(
                    "========= PO Hoplink session ======= Processing of PO commands =======================");
        }
        /* prepare T2 Environment read record */
        ReadRecordsRespPars readT2EnvironmentParser =
                poTransaction.prepareReadRecordsCmd(HoplinkInfo.SFI_T2Environment,
                        ReadDataStructure.SINGLE_RECORD_DATA, HoplinkInfo.RECORD_NUMBER_1,
                        (byte) 0x00, HoplinkInfo.EXTRAINFO_ReadRecord_T2EnvironmentRec1);

        /* prepare T2 Usage read record */
        ReadRecordsRespPars readT2UsageParser =
                poTransaction.prepareReadRecordsCmd(HoplinkInfo.SFI_T2Usage,
                        ReadDataStructure.SINGLE_RECORD_DATA, HoplinkInfo.RECORD_NUMBER_1,
                        (byte) 0x00, HoplinkInfo.EXTRAINFO_ReadRecord_T2UsageRec1);

        poProcessStatus = poTransaction.processPoCommands();

        if (logger.isInfoEnabled()) {
            logger.info(
                    "========= PO Hoplink session ======= Closing ============================");
        }
        poProcessStatus = poTransaction
                .processClosing(PoTransaction.CommunicationMode.CONTACTLESS_MODE, false);

        if (poTransaction.isSuccessful()) {
            if (logger.isInfoEnabled()) {
                logger.info(
                        "========= PO Hoplink session ======= SUCCESS !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        } else {
            logger.error(
                    "========= PO Hoplink session ======= ERROR !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    public SeRequestSet prepareSelection() {
        /*
         * Initialize the selection process for the poReader
         */
        seSelection = new SeSelection(poReader);

        /*
         * Add selection case 1: Fake AID1, protocol ISO, target rev 3
         */
        seSelection
                .prepareSelector(new PoSelector(
                        new SeSelector.SelectionParameters(ByteArrayUtils.fromHex("AABBCCDDEE"),
                                false),
                        true, ContactlessProtocols.PROTOCOL_ISO14443_4,
                        PoSelector.RevisionTarget.TARGET_REV3, "Selector with fake AID1"));
        /*
         * Add selection case 2: Hoplink application, protocol ISO, target rev 3
         *
         * addition of read commands to execute following the selection
         */
        PoSelector poSelectorHoplink =
                new PoSelector(
                        new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(HoplinkInfo.AID),
                                false),
                        true, ContactlessProtocols.PROTOCOL_ISO14443_4,
                        PoSelector.RevisionTarget.TARGET_REV3, "Hoplink selector");

        poSelectorHoplink.prepareReadRecordsCmd(HoplinkInfo.SFI_T2Environment,
                HoplinkInfo.RECORD_NUMBER_1, true, (byte) 0x00,
                HoplinkInfo.EXTRAINFO_ReadRecord_T2EnvironmentRec1);

        seSelection.prepareSelector(poSelectorHoplink);

        /*
         * Add selection case 3: Fake AID2, unspecified protocol, target rev 2 or 3
         */

        seSelection
                .prepareSelector(new PoSelector(
                        new SeSelector.SelectionParameters(ByteArrayUtils.fromHex("EEDDCCBBAA"),
                                false),
                        true, ContactlessProtocols.PROTOCOL_ISO14443_4,
                        PoSelector.RevisionTarget.TARGET_REV2_REV3, "Selector with fake AID2"));

        return seSelection.getSelectionOperation();
    }

    /**
     * Do the PO selection and possibly go on with Hoplink transactions.
     */
    @Override
    public void processSeMatch(SeResponseSet seResponses) {
        if (seSelection.processSelection(seResponses)) {
            MatchingSe selectedSe = seSelection.getSelectedSe();
            try {
                /* first time: check SAM */
                if (!this.samChannelOpen) {
                    /* the following method will throw an exception if the SAM is not available. */
                    SamManagement.checkSamAndOpenChannel(samReader);
                    this.samChannelOpen = true;
                }

                profiler = new Profiler("Entire transaction");

                /* Time measurement */
                profiler.start("Initial selection");

                PoTransaction poTransaction =
                        new PoTransaction(poReader, (CalypsoPo) selectedSe, samReader, samSetting);
                profiler.start("Hoplink1");
                doHoplinkReadWriteTransaction(poTransaction, true);
            } catch (KeypleReaderException ex) {
                logger.error("Selection exception: {}", ex.getMessage());
            }
        } else {
            logger.info("No SE matched the selection");
        }

        profiler.stop();
        logger.warn(System.getProperty("line.separator") + "{}", profiler);
    }

    @Override
    public void processSeInsertion() {
        System.out.println("Unexpected SE insertion event");
    }

    @Override
    public void processSeRemoval() {
        System.out.println("SE removal event");
    }

    @Override
    public void processUnexpectedSeRemoval() {
        System.out.println("Unexpected SE removal event");
    }
}
