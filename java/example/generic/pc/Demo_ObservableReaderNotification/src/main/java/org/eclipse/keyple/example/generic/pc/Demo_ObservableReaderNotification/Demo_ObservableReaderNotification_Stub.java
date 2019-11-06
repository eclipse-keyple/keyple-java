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


public class Demo_ObservableReaderNotification_Stub {
    public final static Object waitBeforeEnd = new Object();

    public static void main(String[] args) throws Exception {
        ObservableReaderNotificationEngine demoEngine = new ObservableReaderNotificationEngine();

        // Set Stub plugin
        SeProxyService seProxyService = SeProxyService.getInstance();

        final String STUB_PLUGIN_NAME = "stub1";

        /* Register Stub plugin in the platform */
        seProxyService.registerPlugin(new StubPluginFactory(STUB_PLUGIN_NAME));
        ReaderPlugin stubPlugin = seProxyService.getPlugin(STUB_PLUGIN_NAME);

        // Set observers
        System.out.println("Set plugin observer.");
        demoEngine.setPluginObserver();

        System.out.println("Wait a little to see the \"no reader available message\".");
        Thread.sleep(200);

        System.out.println("Plug reader 1.");
        ((StubPlugin) stubPlugin).plugStubReader("Reader1", true);

        Thread.sleep(100);

        System.out.println("Plug reader 2.");
        ((StubPlugin) stubPlugin).plugStubReader("Reader2", true);

        Thread.sleep(1000);

        StubReader reader1 = (StubReader) (stubPlugin.getReader("Reader1"));

        StubReader reader2 = (StubReader) (stubPlugin.getReader("Reader2"));

        /* Create 'virtual' Hoplink and SAM SE */
        StubSecureElement se1 = new StubSe1();
        StubSecureElement se2 = new StubSe2();

        System.out.println("Insert SE into reader 1.");
        reader1.insertSe(se1);

        Thread.sleep(100);

        System.out.println("Insert SE into reader 2.");
        reader2.insertSe(se2);

        Thread.sleep(100);

        System.out.println("Remove SE from reader 1.");
        reader1.removeSe();

        Thread.sleep(100);

        System.out.println("Remove SE from reader 2.");
        reader2.removeSe();

        Thread.sleep(100);

        System.out.println("Plug reader 1 again (twice).");
        ((StubPlugin) stubPlugin).plugStubReader("Reader1", true);


        System.out.println("Unplug reader 1.");
        ((StubPlugin) stubPlugin).unplugStubReader("Reader1", true);


        Thread.sleep(100);

        System.out.println("Plug reader 1 again.");
        ((StubPlugin) stubPlugin).plugStubReader("Reader1", true);

        Thread.sleep(100);

        System.out.println("Unplug reader 1.");
        ((StubPlugin) stubPlugin).unplugStubReader("Reader1", true);

        Thread.sleep(100);

        System.out.println("Unplug reader 2.");
        ((StubPlugin) stubPlugin).unplugStubReader("Reader2", true);

        System.out.println("END.");

        System.exit(0);
    }
}
