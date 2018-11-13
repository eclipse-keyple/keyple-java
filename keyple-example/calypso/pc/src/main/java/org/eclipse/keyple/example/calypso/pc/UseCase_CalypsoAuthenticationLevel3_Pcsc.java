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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.example.calypso.common.transaction.SamManagement;
import org.eclipse.keyple.example.generic.common.AbstractReaderObserverEngine;
import org.eclipse.keyple.example.generic.common.ReaderUtilities;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.transaction.MatchingSe;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.transaction.SeSelector;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;



public class UseCase_CalypsoAuthenticationLevel3_Pcsc {
    private static Properties properties;

    static class CalypsoAuthenticationLevel3TransactionEngine extends AbstractReaderObserverEngine {
        private final Logger logger =
                LoggerFactory.getLogger(CalypsoAuthenticationLevel3TransactionEngine.class);

        private final ProxyReader poReader, samReader;
        private SeSelection seSelection;
        private boolean samChannelOpen;

        /* define the SAM parameters to provide when creating PoTransaction */
        final EnumMap<PoTransaction.SamSettings, Byte> samSetting =
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

        public CalypsoAuthenticationLevel3TransactionEngine(ProxyReader poReader,
                ProxyReader samReader) {
            this.poReader = poReader;
            this.samReader = samReader;
        }

        public SeRequestSet prepareSelection() {
            /*
             * Initialize the selection process for the poReader
             */
            seSelection = new SeSelection(poReader);

            /* operate PO selection */
            String poAid = properties.getProperty("po.aid");


            /* AID based selection */
            seSelection.prepareSelector(new PoSelector(
                    new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAid), false), true,
                    null, PoSelector.RevisionTarget.TARGET_REV3, "Calypso selection"));
            return seSelection.getSelectionOperation();
        }

        @Override
        public void processSeMatch(SeResponseSet seResponses) {
            Profiler profiler;
            if (seSelection.processSelection(seResponses)) {
                MatchingSe selectedSe = seSelection.getSelectedSe();
                try {
                    /* first time: check SAM */
                    if (!this.samChannelOpen) {
                        /*
                         * the following method will throw an exception if the SAM is not available.
                         */
                        SamManagement.checkSamAndOpenChannel(samReader);
                        this.samChannelOpen = true;
                    }

                    profiler = new Profiler("Entire transaction");

                    /* Time measurement */
                    profiler.start("Initial selection");

                    profiler.start("Calypso1");

                    PoTransaction poTransaction = new PoTransaction(poReader,
                            (CalypsoPo) selectedSe, samReader, samSetting);
                    /*
                     * Open Session for the debit key
                     */
                    boolean poProcessStatus = poTransaction.processOpening(
                            PoTransaction.ModificationMode.ATOMIC,
                            PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT, (byte) 0, (byte) 0);
                    if (!poTransaction.wasRatified()) {
                        logger.info(
                                "========= Previous Secure Session was not ratified. =====================");
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
                    poProcessStatus = poTransaction.processClosing(
                            PoTransaction.CommunicationMode.CONTACTLESS_MODE, false);
                    profiler.stop();
                    logger.warn(System.getProperty("line.separator") + "{}", profiler);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                logger.info("No SE matched the selection");
            }
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

    private static final Object waitForEnd = new Object();

    public static void main(String[] args)
            throws KeypleBaseException, InterruptedException, IOException {

        final Logger logger =
                LoggerFactory.getLogger(UseCase_CalypsoAuthenticationLevel3_Pcsc.class);

        properties = new Properties();

        String propertiesFileName = "config.properties";

        InputStream inputStream = UseCase_CalypsoAuthenticationLevel3_Pcsc.class.getClassLoader()
                .getResourceAsStream(propertiesFileName);

        if (inputStream != null) {
            properties.load(inputStream);
        } else {
            throw new FileNotFoundException(
                    "property file '" + propertiesFileName + "' not found!");
        }

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Get the instance of the PC/SC plugin */
        PcscPlugin pcscPlugin = PcscPlugin.getInstance();

        /* Assign PcscPlugin to the SeProxyService */
        seProxyService.addPlugin(pcscPlugin);

        /*
         * Get PO and SAM readers. Apply regulars expressions to reader names to select PO / SAM
         * readers. Use the getReader helper method from the transaction engine.
         */
        ProxyReader poReader = ReaderUtilities.getReaderByName(seProxyService,
                properties.getProperty("po.reader.regex"));
        ProxyReader samReader = ReaderUtilities.getReaderByName(seProxyService,
                properties.getProperty("sam.reader.regex"));

        /* Both readers are expected not null */
        if (poReader == samReader || poReader == null || samReader == null) {
            throw new IllegalStateException("Bad PO/SAM setup");
        }

        logger.info("PO Reader  NAME = {}", poReader.getName());
        logger.info("SAM Reader  NAME = {}", samReader.getName());

        /* Set PcSc settings per reader */
        poReader.setParameter(PcscReader.SETTING_KEY_LOGGING, "true");
        poReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T1);
        samReader.setParameter(PcscReader.SETTING_KEY_LOGGING, "true");
        samReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T0);

        /*
         * PC/SC card access mode:
         *
         * The SAM is left in the SHARED mode (by default) to avoid automatic resets due to the
         * limited time between two consecutive exchanges granted by Windows.
         *
         * The PO reader is set to EXCLUSIVE mode to avoid side effects during the selection step
         * that may result in session failures.
         *
         * These two points will be addressed in a coming release of the Keyple PcSc reader plugin.
         */
        samReader.setParameter(PcscReader.SETTING_KEY_MODE, PcscReader.SETTING_MODE_SHARED);
        poReader.setParameter(PcscReader.SETTING_KEY_MODE, PcscReader.SETTING_MODE_SHARED);

        /* Set the PO reader protocol flag */
        poReader.addSeProtocolSetting(
                new SeProtocolSetting(PcscProtocolSetting.SETTING_PROTOCOL_ISO14443_4));

        /* Setting up the transaction engine (implements Observer) */
        CalypsoAuthenticationLevel3TransactionEngine transactionEngine =
                new CalypsoAuthenticationLevel3TransactionEngine(poReader, samReader);

        /* Set the default selection operation */
        ((ObservableReader) poReader).setDefaultSeRequests(transactionEngine.prepareSelection());

        /* Set terminal as Observer of the first reader */
        ((ObservableReader) poReader).addObserver(transactionEngine);

        /* Wait for ever (exit with CTRL-C) */
        synchronized (waitForEnd) {
            waitForEnd.wait();
        }
    }
}
