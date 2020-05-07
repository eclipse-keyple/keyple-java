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
package org.eclipse.keyple.example.generic.pc.Demo_ObservableReaderNotification;

import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.example.common.generic.ObservableReaderNotificationEngine;
import org.eclipse.keyple.example.common.generic.stub.StubSe1;
import org.eclipse.keyple.example.common.generic.stub.StubSe2;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubPluginFactory;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.plugin.stub.StubSecureElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Demo_ObservableReaderNotification_Stub {
    private static final Logger logger =
            LoggerFactory.getLogger(Demo_ObservableReaderNotification_Stub.class);
    public static final Object waitBeforeEnd = new Object();

    public static void main(String[] args) throws Exception {
        ObservableReaderNotificationEngine demoEngine = new ObservableReaderNotificationEngine();

        // Set Stub plugin
        SeProxyService seProxyService = SeProxyService.getInstance();

        final String STUB_PLUGIN_NAME = "stub1";
        final String READER1_NAME = "Reader1";
        final String READER2_NAME = "Reader2";

        /* Register Stub plugin in the platform */
        seProxyService.registerPlugin(new StubPluginFactory(STUB_PLUGIN_NAME));
        ReaderPlugin stubPlugin = seProxyService.getPlugin(STUB_PLUGIN_NAME);

        // Set observers
        logger.info("Set plugin observer.");
        demoEngine.setPluginObserver();

        logger.info("Wait a little to see the \"no reader available message\".");
        Thread.sleep(200);

        logger.info("Plug reader 1.");
        ((StubPlugin) stubPlugin).plugStubReader(READER1_NAME, true);

        Thread.sleep(100);

        logger.info("Plug reader 2.");
        ((StubPlugin) stubPlugin).plugStubReader(READER2_NAME, true);

        Thread.sleep(1000);

        StubReader reader1 = (StubReader) (stubPlugin.getReader(READER1_NAME));

        StubReader reader2 = (StubReader) (stubPlugin.getReader(READER2_NAME));

        /* Create 'virtual' Hoplink and SAM SE */
        StubSecureElement se1 = new StubSe1();
        StubSecureElement se2 = new StubSe2();

        logger.info("Insert SE into reader 1.");
        reader1.insertSe(se1);

        Thread.sleep(100);

        logger.info("Insert SE into reader 2.");
        reader2.insertSe(se2);

        Thread.sleep(100);

        logger.info("Remove SE from reader 1.");
        reader1.removeSe();

        Thread.sleep(100);

        logger.info("Remove SE from reader 2.");
        reader2.removeSe();

        Thread.sleep(100);

        logger.info("Plug reader 1 again (twice).");
        ((StubPlugin) stubPlugin).plugStubReader(READER1_NAME, true);


        logger.info("Unplug reader 1.");
        ((StubPlugin) stubPlugin).unplugStubReader(READER1_NAME, true);


        Thread.sleep(100);

        logger.info("Plug reader 1 again.");
        ((StubPlugin) stubPlugin).plugStubReader(READER1_NAME, true);

        Thread.sleep(100);

        logger.info("Unplug reader 1.");
        ((StubPlugin) stubPlugin).unplugStubReader(READER1_NAME, true);

        Thread.sleep(100);

        logger.info("Unplug reader 2.");
        ((StubPlugin) stubPlugin).unplugStubReader(READER2_NAME, true);

        logger.info("END.");

        System.exit(0);
    }
}
