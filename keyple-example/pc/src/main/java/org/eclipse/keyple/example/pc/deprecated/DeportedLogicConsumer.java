/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.pc.deprecated;

import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.example.common.deprecated.BasicCardAccessManager;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReadersPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;

/**
 * @deprecated
 */
public class DeportedLogicConsumer {
    public static void main(String[] args) throws Exception {
        SeProxyService seProxyService = SeProxyService.getInstance();
        System.out.println("SeProxyServ v" + seProxyService.getVersion());
        SortedSet<ReadersPlugin> plugins = new ConcurrentSkipListSet<ReadersPlugin>();
        plugins.add(PcscPlugin.getInstance().setLogging(true));
        seProxyService.setPlugins(plugins);
        for (ReadersPlugin rp : seProxyService.getPlugins()) {
            System.out.println("Reader plugin: " + rp.getName());
            for (ProxyReader pr : rp.getReaders()) {
                System.out
                        .println("Reader name: " + pr.getName() + ", present: " + pr.isSePresent());
                if (pr.isSePresent()) {
                    // This is what contains the actual test logic
                    BasicCardAccessManager mgr = new BasicCardAccessManager();
                    mgr.setPoReader(pr);
                    mgr.run();
                }
            }
        }
    }
}
