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

import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.example.common.calypso.Demo_HoplinkTransactionEngine;
import org.eclipse.keyple.example.pc.calypso.stub.se.*;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.eclipse.keyple.plugin.stub.*;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demo_Hoplink_Stub {

    /**
     * main program entry
     *
     * @param args the program arguments
     * @throws InterruptedException thread exception
     */
    public static void main(String[] args) throws InterruptedException {
        final Logger logger = LoggerFactory.getLogger(Demo_Hoplink_Stub.class);

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        logger.debug("SeProxyService version : {}", seProxyService.getVersion());

        SortedSet<ReaderPlugin> pluginsSet = new ConcurrentSkipListSet<ReaderPlugin>();

        StubPlugin stubPlugin = StubPlugin.getInstance();

        /* Get the instance of the PcscPlugin (Singleton pattern) */
        pluginsSet.add(stubPlugin);

        /* Assign StubPlugin to the SeProxyService */
        seProxyService.setPlugins(pluginsSet);

        /* Setting up the transaction engine (implements Observer) */
        Demo_HoplinkTransactionEngine transactionEngine = new Demo_HoplinkTransactionEngine();

        /*
         * Plug PO and CSM stub readers.
         */
        stubPlugin.plugStubReader("poReader");
        stubPlugin.plugStubReader("csmReader");

        Thread.sleep(200);

        StubReader poReader = null, csmReader = null;
        try {
            poReader = (StubReader) (stubPlugin.getReader("poReader"));
            csmReader = (StubReader) (stubPlugin.getReader("csmReader"));
        } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
        }

        /* Both readers are expected not null */
        if (poReader == csmReader || poReader == null || csmReader == null) {
            throw new IllegalStateException("Bad PO/CSM setup");
        }

        logger.info("PO Reader  NAME = {}", poReader.getName());
        logger.info("CSM Reader  NAME = {}", csmReader.getName());

        /* Set the PO reader protocol flag */
        poReader.addSeProtocolSetting(
                new SeProtocolSetting(PcscProtocolSetting.SETTING_PROTOCOL_ISO14443_4));

        /* Assign readers to the Hoplink transaction engine */
        transactionEngine.setReaders(poReader, csmReader);

        /* Create 'virtual' Hoplink and CSM SE */
        StubSecureElement hoplinkSE = new StubHoplink();
        StubSecureElement csmSE = new StubCsmHoplink();

        /* Insert the CSM into the CSM reader */
        logger.info("Insert stub CSM SE.");
        csmReader.insertSe(csmSE);

        /* Set the transactionEngine as Observer of the PO reader */
        ((ObservableReader) poReader).addObserver(transactionEngine);

        /* Wait a little the time the thread starts. */
        Thread.sleep(10);

        logger.info("Insert stub PO SE.");
        poReader.insertSe(hoplinkSE);

        /* Wait a while the time that the transaction ends. */
        Thread.sleep(1000);

        /* Remove SE */
        logger.info("Remove stub CSM and PO SE.");

        poReader.removeSe();
        csmReader.removeSe();

        logger.info("END.");

        System.exit(0);
    }
}
