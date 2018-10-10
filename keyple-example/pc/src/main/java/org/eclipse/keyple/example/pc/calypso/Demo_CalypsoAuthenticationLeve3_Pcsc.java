/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.pc.calypso;

import static org.eclipse.keyple.calypso.transaction.PoSecureSession.*;
import static org.eclipse.keyple.calypso.transaction.PoSecureSession.CommunicationMode.CONTACTLESS_MODE;
import static org.eclipse.keyple.calypso.transaction.PoSecureSession.CsmSettings.*;
import static org.eclipse.keyple.calypso.transaction.PoSecureSession.ModificationMode.ATOMIC;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import org.eclipse.keyple.calypso.transaction.CalypsoPO;
import org.eclipse.keyple.calypso.transaction.PoSecureSession;
import org.eclipse.keyple.example.common.calypso.CalypsoBasicInfoAndSampleCommands;
import org.eclipse.keyple.example.common.generic.DemoHelpers;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;

public class Demo_CalypsoAuthenticationLeve3_Pcsc {
    private static Properties properties;

    static class CalypsoAuthenticationLeve3TransactionEngine
            implements ObservableReader.ReaderObserver {
        private final Logger logger =
                LoggerFactory.getLogger(CalypsoAuthenticationLeve3TransactionEngine.class);

        private final ProxyReader poReader, csmReader;
        private boolean csmChannelOpen;

        /* define the CSM parameters to provide when creating PoSecureSession */
        final EnumMap<PoSecureSession.CsmSettings, Byte> csmSetting =
                new EnumMap<PoSecureSession.CsmSettings, Byte>(PoSecureSession.CsmSettings.class) {
                    {
                        put(CS_DEFAULT_KIF_PERSO, DEFAULT_KIF_PERSO);
                        put(CS_DEFAULT_KIF_LOAD, DEFAULT_KIF_LOAD);
                        put(CS_DEFAULT_KIF_DEBIT, DEFAULT_KIF_DEBIT);
                        put(CS_DEFAULT_KEY_RECORD_NUMBER, DEFAULT_KEY_RECORD_NUMER);
                    }
                };

        public CalypsoAuthenticationLeve3TransactionEngine(ProxyReader poReader,
                ProxyReader csmReader) {
            this.poReader = poReader;
            this.csmReader = csmReader;
        }

        @Override
        public void update(ReaderEvent event) {
            switch (event.getEventType()) {
                case SE_INSERTED:
                    if (logger.isInfoEnabled()) {
                        logger.info("SE INSERTED");
                        logger.info("Start processing of a Calypso PO");
                    }
                    operatePoTransactions();
                    break;
                case SE_REMOVAL:
                    if (logger.isInfoEnabled()) {
                        logger.info("SE REMOVED");
                        logger.info("Wait for Calypso PO");
                    }
                    break;
                default:
                    logger.error("IO Error");
            }
        }

        private void checkCsmAndOpenChannel() {
            /*
             * check the availability of the CSM, open its physical and logical channels and keep it
             * open
             */
            String csmATRregex = properties.getProperty("csm.atr.regex"); // csm

            /* open CSM logical channel */
            SeRequest csmCheckRequest =
                    new SeRequest(new SeRequest.AtrSelector(csmATRregex), null, true);
            SeResponse csmCheckResponse = null;
            try {
                csmCheckResponse =
                        csmReader.transmit(new SeRequestSet(csmCheckRequest)).getSingleResponse();
                if (csmCheckResponse == null) {
                    throw new IllegalStateException("Unable to open a logical channel for CSM!");
                } else {
                }
            } catch (KeypleReaderException e) {
                throw new IllegalStateException("Reader exception: " + e.getMessage());
            }
        }

        private void operatePoTransactions() {
            PoSecureSession.SessionAccessLevel accessLevel =
                    PoSecureSession.SessionAccessLevel.SESSION_LVL_DEBIT;

            Profiler profiler;
            try {
                /* first time: check CSM */
                if (!this.csmChannelOpen) {
                    /* the following method will throw an exception if the CSM is not available. */
                    checkCsmAndOpenChannel();
                    this.csmChannelOpen = true;
                }

                profiler = new Profiler("Entire transaction");

                /* operate PO selection */
                String poAid = properties.getProperty("po.aid");

                /*
                 * Prepare the PO selection SeRequestSet
                 *
                 * Create a SeRequest list with various selection cases.
                 */
                Set<SeRequest> selectionRequests = new LinkedHashSet<SeRequest>();

                /* AID based selection */
                SeRequest seRequest = new SeRequest(
                        new SeRequest.AidSelector(ByteArrayUtils.fromHex(poAid)), null, true,
                        CalypsoBasicInfoAndSampleCommands.selectApplicationSuccessfulStatusCodes);

                selectionRequests.add(seRequest);

                /* Time measurement */
                profiler.start("Initial selection");

                List<SeResponse> seResponses =
                        poReader.transmit(new SeRequestSet(selectionRequests)).getResponses();

                /*
                 * If the Calypso selection succeeded we should have 2 responses and the 2nd one not
                 * null
                 */
                if (seResponses.size() == 1 && seResponses.get(0) != null) {

                    profiler.start("Calypso1");

                    PoSecureSession poTransaction = new PoSecureSession(poReader, csmReader,
                            csmSetting, new CalypsoPO(seResponses.get(0)));
                    /*
                     * Open Session for the debit key
                     */
                    SeResponse seResponse = poTransaction.processOpening(ATOMIC, accessLevel,
                            (byte) 0, (byte) 0, null);
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
                    seResponse = poTransaction.processClosing(null, CONTACTLESS_MODE, false);
                } else {
                    logger.error(
                            "No Calypso transaction. SeResponse to Calypso selection was null.");
                }
                profiler.stop();
                logger.warn(System.getProperty("line.separator") + "{}", profiler);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static final Object waitForEnd = new Object();

    public static void main(String[] args)
            throws KeypleBaseException, InterruptedException, IOException {

        final Logger logger = LoggerFactory.getLogger(Demo_CalypsoBasic_Pcsc.class);

        properties = new Properties();

        String propertiesFileName = "config.properties";

        InputStream inputStream = Demo_CalypsoAuthenticationLeve3_Pcsc.class.getClassLoader()
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
         * Get PO and CSM readers. Apply regulars expressions to reader names to select PO / CSM
         * readers. Use the getReader helper method from the transaction engine.
         */
        ProxyReader poReader = DemoHelpers.getReaderByName(seProxyService,
                properties.getProperty("po.reader.regex"));
        ProxyReader csmReader = DemoHelpers.getReaderByName(seProxyService,
                properties.getProperty("csm.reader.regex"));

        /* Both readers are expected not null */
        if (poReader == csmReader || poReader == null || csmReader == null) {
            throw new IllegalStateException("Bad PO/CSM setup");
        }

        logger.info("PO Reader  NAME = {}", poReader.getName());
        logger.info("CSM Reader  NAME = {}", csmReader.getName());

        /* Set PcSc settings per reader */
        poReader.setParameter(PcscReader.SETTING_KEY_LOGGING, "true");
        poReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T1);
        csmReader.setParameter(PcscReader.SETTING_KEY_LOGGING, "true");
        csmReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T0);

        /*
         * PC/SC card access mode:
         *
         * The CSM is left in the SHARED mode (by default) to avoid automatic resets due to the
         * limited time between two consecutive exchanges granted by Windows.
         *
         * The PO reader is set to EXCLUSIVE mode to avoid side effects during the selection step
         * that may result in session failures.
         *
         * These two points will be addressed in a coming release of the Keyple PcSc reader plugin.
         */
        csmReader.setParameter(PcscReader.SETTING_KEY_MODE, PcscReader.SETTING_MODE_SHARED);
        poReader.setParameter(PcscReader.SETTING_KEY_MODE, PcscReader.SETTING_MODE_SHARED);

        /* Set the PO reader protocol flag */
        poReader.addSeProtocolSetting(
                new SeProtocolSetting(PcscProtocolSetting.SETTING_PROTOCOL_ISO14443_4));

        /* Setting up the transaction engine (implements Observer) */
        CalypsoAuthenticationLeve3TransactionEngine transactionEngine =
                new CalypsoAuthenticationLeve3TransactionEngine(poReader, csmReader);

        /* Set terminal as Observer of the first reader */
        ((ObservableReader) poReader).addObserver(transactionEngine);

        /* Wait for ever (exit with CTRL-C) */
        synchronized (waitForEnd) {
            waitForEnd.wait();
        }
    }
}
