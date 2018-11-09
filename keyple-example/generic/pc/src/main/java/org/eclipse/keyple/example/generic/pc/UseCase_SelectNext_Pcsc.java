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
package org.eclipse.keyple.example.generic.pc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.eclipse.keyple.example.generic.common.AbstractSelectionEngine;
import org.eclipse.keyple.example.generic.common.ReaderUtilities;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.transaction.MatchingSe;
import org.eclipse.keyple.transaction.SeSelector;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The UseCase_SelectNext_Pcsc class illustrates the use of the select next mechanism
 */
public class UseCase_SelectNext_Pcsc {

    static class SelectNextEngine extends AbstractSelectionEngine {
        private final Logger logger = LoggerFactory.getLogger(SelectNextEngine.class);

        private final ProxyReader poReader;

        public SelectNextEngine(ProxyReader poReader) {
            this.poReader = poReader;
        }

        @Override
        public void prepareSelection() {
            /* operate SE selection */
            String poAidPrefix = "A000000404012509";

            initializeSelection(poReader);

            /* AID based selection */
            prepareSelector(new SeSelector(
                    new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix), false),
                    false, null, "Initial selection"));
            /* next selection */
            prepareSelector(new SeSelector(
                    new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix), true),
                    false, null, "Next selection #1"));
            /* next selection */
            prepareSelector(new SeSelector(
                    new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix), true),
                    false, null, "Next selection #2"));
            /* next selection */
            prepareSelector(new SeSelector(
                    new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix), true),
                    false, null, "Next selection #3"));
            /* next selection */
            prepareSelector(new SeSelector(
                    new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix), true),
                    false, null, "Next selection #4"));
            /* next selection */
            prepareSelector(new SeSelector(
                    new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix), true),
                    false, null, "Next selection #5"));
            /* next selection */
            prepareSelector(new SeSelector(
                    new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix), true),
                    false, null, "Next selection #6"));
            /* next selection */
            prepareSelector(new SeSelector(
                    new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix), true),
                    false, null, "Next selection #7"));
            /* next selection */
            prepareSelector(new SeSelector(
                    new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix), true),
                    false, null, "Next selection #8"));
            /* next selection */
            prepareSelector(new SeSelector(
                    new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix), true),
                    false, null, "Next selection #9"));
            /* next selection */
            prepareSelector(new SeSelector(
                    new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix), true),
                    false, null, "Next selection #10"));
            /* next selection */
            prepareSelector(new SeSelector(
                    new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix), true),
                    false, null, "Next selection #11"));
            /* next selection */
            prepareSelector(new SeSelector(
                    new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix), true),
                    false, null, "Next selection #12"));
        }

        public void operateSeTransaction(MatchingSe selectedSe) {
            if (selectedSe != null) {
                logger.info("Selection: {}", selectedSe.getSelectionSeResponse());
            } else {
                logger.info("The selection process did not return any selected SE.");
            }
        }

        @Override
        public void operateSeRemoval() {

        }
    }

    private static final Object waitForEnd = new Object();

    public static void main(String[] args)
            throws KeypleBaseException, InterruptedException, IOException {

        final Logger logger = LoggerFactory.getLogger(UseCase_SelectNext_Pcsc.class);

        Properties properties = new Properties();

        String propertiesFileName = "config.properties";

        InputStream inputStream = UseCase_SelectNext_Pcsc.class.getClassLoader()
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
         * Get PO reader. Apply regulars expressions to reader names to select the PO reader.
         */
        ProxyReader poReader = ReaderUtilities.getReaderByName(seProxyService,
                properties.getProperty("po.reader.regex"));

        /* Both readers are expected not null */
        if (poReader == null) {
            throw new IllegalStateException("Bad PO reader setup");
        }

        logger.info("PO Reader  NAME = {}", poReader.getName());

        /* Set PcSc settings per reader */
        poReader.setParameter(PcscReader.SETTING_KEY_LOGGING, "true");
        poReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T1);

        /*
         * PC/SC card access mode:
         *
         * The PO reader is set to EXCLUSIVE mode to avoid side effects during the selection step
         * that may result in session failures.
         *
         * These two points will be addressed in a coming release of the Keyple PcSc reader plugin.
         */
        poReader.setParameter(PcscReader.SETTING_KEY_MODE, PcscReader.SETTING_MODE_SHARED);

        /* Set the PO reader protocol flag */
        poReader.addSeProtocolSetting(
                new SeProtocolSetting(PcscProtocolSetting.SETTING_PROTOCOL_ISO14443_4));

        /* Setting up the transaction engine (implements Observer) */
        SelectNextEngine transactionEngine = new SelectNextEngine(poReader);

        /* Set terminal as Observer of the first reader */
        ((ObservableReader) poReader).addObserver(transactionEngine);

        /* Wait for ever (exit with CTRL-C) */
        synchronized (waitForEnd) {
            waitForEnd.wait();
        }
    }
}
