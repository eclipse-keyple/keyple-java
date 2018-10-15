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

package org.eclipse.keyple.example.pc.calypso;


import org.eclipse.keyple.example.common.calypso.Demo_CalypsoBasicTransactionEngine;
import org.eclipse.keyple.example.common.generic.DemoHelpers;
import org.eclipse.keyple.example.pc.generic.PcscReadersSettings;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demo_CalypsoBasic_Pcsc {
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
        Logger logger = LoggerFactory.getLogger(Demo_CalypsoBasic_Pcsc.class);

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Get the instance of the PC/SC plugin */
        PcscPlugin pcscPlugin = PcscPlugin.getInstance();

        /* Assign PcscPlugin to the SeProxyService */
        seProxyService.addPlugin(pcscPlugin);

        /* Setting up the transaction engine (implements Observer) */
        Demo_CalypsoBasicTransactionEngine transactionEngine =
                new Demo_CalypsoBasicTransactionEngine();

        /*
         * Get PO and CSM readers. Apply regulars expressions to reader names to select PO / CSM
         * readers. Use the getReader helper method from the transaction engine.
         */
        ProxyReader poReader = null, csmReader = null;
        try {
            poReader = DemoHelpers.getReaderByName(seProxyService,
                    PcscReadersSettings.PO_READER_NAME_REGEX);
            csmReader = DemoHelpers.getReaderByName(seProxyService,
                    PcscReadersSettings.CSM_READER_NAME_REGEX);
        } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
        }

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
         * This point will be addressed in a coming release of the Keyple PcSc reader plugin.
         *
         * The PO reader is set to EXCLUSIVE mode to avoid side effects (on OS Windows 8+) during
         * the selection step that may result in session failures.
         *
         * See KEYPLE-CORE.PC.md file to learn more about this point.
         *
         */
        csmReader.setParameter(PcscReader.SETTING_KEY_MODE, PcscReader.SETTING_MODE_SHARED);
        poReader.setParameter(PcscReader.SETTING_KEY_MODE, PcscReader.SETTING_MODE_SHARED);

        /* Set the PO reader protocol flag */
        poReader.addSeProtocolSetting(
                new SeProtocolSetting(PcscProtocolSetting.SETTING_PROTOCOL_ISO14443_4));
        poReader.addSeProtocolSetting(
                new SeProtocolSetting(PcscProtocolSetting.SETTING_PROTOCOL_B_PRIME));
        /* Assign readers to Calypso transaction engine */
        transactionEngine.setReaders(poReader, csmReader);

        /* Set terminal as Observer of the first reader */
        ((ObservableReader) poReader).addObserver(transactionEngine);

        /* Wait for ever (exit with CTRL-C) */
        synchronized (waitForEnd) {
            waitForEnd.wait();
        }
    }
}
