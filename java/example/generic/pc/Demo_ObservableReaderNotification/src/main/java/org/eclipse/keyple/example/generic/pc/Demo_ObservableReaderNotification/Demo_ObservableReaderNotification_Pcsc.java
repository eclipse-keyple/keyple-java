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


import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.example.common.generic.ObservableReaderNotificationEngine;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;


public class Demo_ObservableReaderNotification_Pcsc {
    public final static Object waitBeforeEnd = new Object();

    public static void main(String[] args) throws Exception {
        ObservableReaderNotificationEngine demoEngine = new ObservableReaderNotificationEngine();

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Assign PcscPlugin to the SeProxyService */
        seProxyService.registerPlugin(new PcscPluginFactory());

        /* Set observers */
        demoEngine.setPluginObserver();

        System.out.println("Wait for reader or SE insertion/removal");

        /* Wait indefinitely. CTRL-C to exit. */
        synchronized (waitBeforeEnd) {
            waitBeforeEnd.wait();
        }
    }
}
