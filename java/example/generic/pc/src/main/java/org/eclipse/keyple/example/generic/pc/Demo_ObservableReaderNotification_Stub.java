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
package org.eclipse.keyple.example.generic.pc;

import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.example.generic.common.ObservableReaderNotificationEngine;
import org.eclipse.keyple.example.generic.pc.stub.se.StubSe1;
import org.eclipse.keyple.example.generic.pc.stub.se.StubSe2;
import org.eclipse.keyple.plugin.stub.StubPluginFactory;
import org.eclipse.keyple.plugin.stub.StubSecureElement;


public class Demo_ObservableReaderNotification_Stub {
    private final static String READER_1 = "Reader1";
    private final static String READER_2 = "Reader2";

    public final static Object waitBeforeEnd = new Object();

    public static void main(String[] args) throws Exception {
        ObservableReaderNotificationEngine demoEngine = new ObservableReaderNotificationEngine();

        // Set Stub plugin
        SeProxyService seProxyService = SeProxyService.getInstance();
        SortedSet<ReaderPlugin> pluginsSet = new ConcurrentSkipListSet<ReaderPlugin>();

        StubPluginFactory stubPluginFactory = StubPluginFactory.getInstance();

        ReaderPlugin stubPlugin = stubPluginFactory.getPluginInstance();

        pluginsSet.add(stubPlugin);
        seProxyService.setPlugins(pluginsSet);

        // Set observers
        System.out.println("Set plugin observer.");
        demoEngine.setPluginObserver();

        System.out.println("Wait a little to see the \"no reader available message\".");
        Thread.sleep(200);

        System.out.println("Plug reader 1.");
        stubPluginFactory.plugStubReader("Reader1", true);

        Thread.sleep(100);

        System.out.println("Plug reader 2.");
        stubPluginFactory.plugStubReader("Reader2", true);

        Thread.sleep(1000);

        SeReader reader1 = stubPlugin.getReader("Reader1");

        SeReader reader2 = stubPlugin.getReader("Reader2");

        /* Create 'virtual' Hoplink and SAM SE */
        StubSecureElement se1 = new StubSe1();
        StubSecureElement se2 = new StubSe2();

        System.out.println("Insert SE into reader 1.");
        stubPluginFactory.insertSe(READER_1, se1);

        Thread.sleep(100);

        System.out.println("Insert SE into reader 2.");
        stubPluginFactory.insertSe(READER_2, se2);

        Thread.sleep(100);

        System.out.println("Remove SE from reader 1.");
        stubPluginFactory.removeSe(READER_1);

        Thread.sleep(100);

        System.out.println("Remove SE from reader 2.");
        stubPluginFactory.removeSe(READER_2);

        Thread.sleep(100);

        System.out.println("Plug reader 1 again (twice).");
        stubPluginFactory.plugStubReader("Reader1", true);

        System.out.println("Unplug reader 1.");
        stubPluginFactory.unplugStubReader("Reader1", true);

        Thread.sleep(100);

        System.out.println("Plug reader 1 again.");
        stubPluginFactory.plugStubReader("Reader1", true);

        Thread.sleep(100);

        System.out.println("Unplug reader 2.");
        stubPluginFactory.unplugStubReader("Reader2", true);

        Thread.sleep(100);

        System.out.println("Unplug reader 2.");
        stubPluginFactory.unplugStubReader("Reader1", true);

        System.out.println("END.");

        System.exit(0);
    }
}
