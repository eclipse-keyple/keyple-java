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


import org.eclipse.keyple.example.calypso.pc.transaction.CalypsoClassicTransactionEngine;
import org.eclipse.keyple.example.generic.pc.ReaderUtilities;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.SeReader;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demo_CalypsoClassic_Pcsc {
    /**
     * This object is used to freeze the main thread while card operations are handle through the
     * observers callbacks. A call to the notify() method would end the program (not demonstrated
     * here).
     */
    private static final Object waitForEnd = new Object();

    /**
     * main program entry
     *
     * @param args the program arguments
     * @throws KeypleBaseException setParameter exception
     * @throws InterruptedException thread exception
     */
    public static void main(String[] args) throws KeypleBaseException, InterruptedException {
        Logger logger = LoggerFactory.getLogger(Demo_CalypsoClassic_Pcsc.class);

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Get the instance of the PC/SC plugin */
        PcscPlugin pcscPlugin = PcscPlugin.getInstance();

        /* Assign PcscPlugin to the SeProxyService */
        seProxyService.addPlugin(pcscPlugin);

        /* Setting up the transaction engine (implements Observer) */
        CalypsoClassicTransactionEngine transactionEngine = new CalypsoClassicTransactionEngine();

        /*
         * Get PO and SAM readers. Apply regulars expressions to reader names to select PO / SAM
         * readers. Use the getReader helper method from the transaction engine.
         */
        SeReader poReader = null, samReader = null;
        try {
            poReader = ReaderUtilities.getReaderByName(seProxyService,
                    PcscReadersSettings.PO_READER_NAME_REGEX);
            samReader = ReaderUtilities.getReaderByName(seProxyService,
                    PcscReadersSettings.SAM_READER_NAME_REGEX);
        } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
        }

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
         * This point will be addressed in a coming release of the Keyple PcSc reader plugin.
         *
         * The PO reader is set to EXCLUSIVE mode to avoid side effects (on OS Windows 8+) during
         * the selection step that may result in session failures.
         *
         * See KEYPLE-CORE.PC.md file to learn more about this point.
         *
         */
        samReader.setParameter(PcscReader.SETTING_KEY_MODE, PcscReader.SETTING_MODE_SHARED);
        poReader.setParameter(PcscReader.SETTING_KEY_MODE, PcscReader.SETTING_MODE_SHARED);

        /* Set the PO reader protocol flag */
        poReader.addSeProtocolSetting(
                new SeProtocolSetting(PcscProtocolSetting.SETTING_PROTOCOL_ISO14443_4));
        poReader.addSeProtocolSetting(
                new SeProtocolSetting(PcscProtocolSetting.SETTING_PROTOCOL_B_PRIME));

        /* Assign the readers to the Calypso transaction engine */
        transactionEngine.setReaders(poReader, samReader);

        /* Set the default selection operation */
        ((ObservableReader) poReader).setDefaultSelectionRequest(
                transactionEngine.preparePoSelection(),
                ObservableReader.NotificationMode.MATCHED_ONLY);

        /* Set terminal as Observer of the first reader */
        ((ObservableReader) poReader).addObserver(transactionEngine);

        /* Wait for ever (exit with CTRL-C) */
        synchronized (waitForEnd) {
            waitForEnd.wait();
        }
    }
}
