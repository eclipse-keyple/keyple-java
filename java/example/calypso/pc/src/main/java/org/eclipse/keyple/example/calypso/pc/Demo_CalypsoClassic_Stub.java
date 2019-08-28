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

import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.example.calypso.common.stub.se.StubCalypsoClassic;
import org.eclipse.keyple.example.calypso.common.stub.se.StubSamCalypsoClassic;
import org.eclipse.keyple.example.calypso.pc.transaction.CalypsoClassicTransactionEngine;
import org.eclipse.keyple.plugin.stub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demo_CalypsoClassic_Stub {

    /**
     * main program entry
     *
     * @param args the program arguments
     * @throws InterruptedException thread exception
     */
    public static void main(String[] args) throws InterruptedException, KeyplePluginNotFoundException {
        final Logger logger = LoggerFactory.getLogger(Demo_CalypsoClassic_Stub.class);

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Register  Stub plugin in the platform */
        seProxyService.registerPlugin(new StubPluginFactory());
        ReaderPlugin stubPlugin = seProxyService.getPlugin(StubPlugin.PLUGIN_NAME);

        /* Setting up the transaction engine (implements Observer) */
        CalypsoClassicTransactionEngine transactionEngine = new CalypsoClassicTransactionEngine();

        /*
         * Plug PO and SAM stub readers.
         */
        ((StubPlugin)stubPlugin).plugStubReader("poReader", true);
        ((StubPlugin)stubPlugin).plugStubReader("samReader", true);

        StubReaderImpl poReader = null, samReader = null;
        try {
            poReader = (StubReaderImpl) (stubPlugin.getReader("poReader"));
            samReader = (StubReaderImpl) (stubPlugin.getReader("samReader"));
        } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
        }

        /* Both readers are expected not null */
        if (poReader == samReader || poReader == null || samReader == null) {
            throw new IllegalStateException("Bad PO/SAM setup");
        }

        logger.info("PO Reader  NAME = {}", poReader.getName());
        logger.info("SAM Reader  NAME = {}", samReader.getName());

        /* Set the PO reader protocol flag */
        poReader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4,
                StubProtocolSetting.STUB_PROTOCOL_SETTING
                        .get(SeCommonProtocols.PROTOCOL_ISO14443_4));
        poReader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_B_PRIME,
                StubProtocolSetting.STUB_PROTOCOL_SETTING.get(SeCommonProtocols.PROTOCOL_B_PRIME));

        /* Assign readers to the Hoplink transaction engine */
        transactionEngine.setReaders(poReader, samReader);

        /* Create 'virtual' Hoplink and SAM SE */
        StubSecureElement calypsoStubSe = new StubCalypsoClassic();
        StubSecureElement samSE = new StubSamCalypsoClassic();

        /* Insert the SAM into the SAM reader */
        logger.info("Insert stub SAM SE.");
        samReader.insertSe(samSE);

        /* Set the default selection operation */
        ((ObservableReader) poReader).setDefaultSelectionRequest(
                transactionEngine.preparePoSelection(),
                ObservableReader.NotificationMode.MATCHED_ONLY);

        /* Set the transactionEngine as Observer of the PO reader */
        ((ObservableReader) poReader).addObserver(transactionEngine);

        logger.info("Insert stub PO SE.");
        poReader.insertSe(calypsoStubSe);

        Thread.sleep(1000);

        /* Remove SE */
        logger.info("Remove stub SAM and PO SE.");

        poReader.removeSe();
        samReader.removeSe();

        logger.info("END.");

        System.exit(0);
    }
}
