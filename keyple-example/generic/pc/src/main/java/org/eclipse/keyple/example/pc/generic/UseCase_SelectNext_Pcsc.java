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
package org.eclipse.keyple.example.pc.generic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.example.common.generic.AbstractTransactionEngine;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeProxyService;
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

/**
 * The UseCase_SelectNext_Pcsc class illustrates the use of the select next mechanism
 */
public class UseCase_SelectNext_Pcsc extends AbstractTransactionEngine {
    private static Properties properties;

    @Override
    public void operateSeTransaction() {

    }

    @SuppressWarnings("unused")
    static class SelectNextEngine extends AbstractTransactionEngine
            implements ObservableReader.ReaderObserver {
        private final Logger logger = LoggerFactory.getLogger(SelectNextEngine.class);

        private final ProxyReader poReader;

        public SelectNextEngine(ProxyReader poReader) {
            this.poReader = poReader;
        }

        public void operateSeTransaction() {
            Profiler profiler;
            try {

                profiler = new Profiler("Entire transaction");

                /* operate PO selection */
                String poAidPrefix = "A000000404012509";

                /*
                 * Prepare the selection using the SeSelection class
                 */
                SeSelection seSelection = new SeSelection(poReader);

                /* AID based selection */
                seSelection.prepareSelector(new PoSelector(
                        new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix),
                                false),
                        false, null, PoSelector.RevisionTarget.TARGET_REV3, "Initial selection"));
                /* next selection */
                seSelection.prepareSelector(new PoSelector(
                        new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix),
                                true),
                        false, null, PoSelector.RevisionTarget.TARGET_REV3, "Next selection #1"));
                /* next selection */
                seSelection.prepareSelector(new PoSelector(
                        new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix),
                                true),
                        false, null, PoSelector.RevisionTarget.TARGET_REV3, "Next selection #2"));
                /* next selection */
                seSelection.prepareSelector(new PoSelector(
                        new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix),
                                true),
                        false, null, PoSelector.RevisionTarget.TARGET_REV3, "Next selection #3"));
                /* next selection */
                seSelection.prepareSelector(new PoSelector(
                        new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix),
                                true),
                        false, null, PoSelector.RevisionTarget.TARGET_REV3, "Next selection #4"));
                /* next selection */
                seSelection.prepareSelector(new PoSelector(
                        new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix),
                                true),
                        false, null, PoSelector.RevisionTarget.TARGET_REV3, "Next selection #5"));
                /* next selection */
                seSelection.prepareSelector(new PoSelector(
                        new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix),
                                true),
                        false, null, PoSelector.RevisionTarget.TARGET_REV3, "Next selection #6"));
                /* next selection */
                seSelection.prepareSelector(new PoSelector(
                        new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix),
                                true),
                        false, null, PoSelector.RevisionTarget.TARGET_REV3, "Next selection #7"));
                /* next selection */
                seSelection.prepareSelector(new PoSelector(
                        new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix),
                                true),
                        false, null, PoSelector.RevisionTarget.TARGET_REV3, "Next selection #8"));
                /* next selection */
                seSelection.prepareSelector(new PoSelector(
                        new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix),
                                true),
                        false, null, PoSelector.RevisionTarget.TARGET_REV3, "Next selection #9"));
                /* next selection */
                seSelection.prepareSelector(new PoSelector(
                        new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix),
                                true),
                        false, null, PoSelector.RevisionTarget.TARGET_REV3, "Next selection #10"));
                /* next selection */
                seSelection.prepareSelector(new PoSelector(
                        new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix),
                                true),
                        false, null, PoSelector.RevisionTarget.TARGET_REV3, "Next selection #11"));
                /* next selection */
                seSelection.prepareSelector(new PoSelector(
                        new SeSelector.SelectionParameters(ByteArrayUtils.fromHex(poAidPrefix),
                                true),
                        false, null, PoSelector.RevisionTarget.TARGET_REV3, "Next selection #12"));

                /* Time measurement */
                profiler.start("Initial selection");

                if (seSelection.processSelection()) {
                    List<MatchingSe> matchingSeList = seSelection.getMatchingSeList();
                    for (MatchingSe matchingSe : matchingSeList) {
                        logger.info("Selection: {}", matchingSe.getSelectionSeResponse());
                    }
                } else {
                    logger.error("Selection failed. SeResponse to selection was null.");
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

        final Logger logger = LoggerFactory.getLogger(UseCase_SelectNext_Pcsc.class);

        properties = new Properties();

        String propertiesFileName = "generic/pc/src/main/resources/config.properties";

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
        ProxyReader poReader =
                getReaderByName(seProxyService, properties.getProperty("po.reader.regex"));

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
