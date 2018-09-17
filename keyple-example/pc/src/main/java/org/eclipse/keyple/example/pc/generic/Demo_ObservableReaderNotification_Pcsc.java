/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.pc.generic;

import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.example.common.generic.Demo_ObservableReaderNotificationEngine;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.seproxy.*;


public class Demo_ObservableReaderNotification_Pcsc {
    public final static Object waitBeforeEnd = new Object();

    public static void main(String[] args) throws Exception {
        Demo_ObservableReaderNotificationEngine demoEngine =
                new Demo_ObservableReaderNotificationEngine();

        /* Set PCSC plugin */
        SeProxyService seProxyService = SeProxyService.getInstance();
        SortedSet<ReaderPlugin> pluginsSet = new ConcurrentSkipListSet<ReaderPlugin>();
        pluginsSet.add(PcscPlugin.getInstance());
        seProxyService.setPlugins(pluginsSet);

        /* Set observers */
        demoEngine.setPluginObserver();

        System.out.println("Wait for reader or SE insertion/removal");

        /* Wait indefinitely. CTRL-C to exit. */
        synchronized (waitBeforeEnd) {
            waitBeforeEnd.wait();
        }
    }
}
