/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.examples.pc;

import java.util.Collections;
import org.keyple.example.common.BasicCardAccessManager;
import org.keyple.plugin.pcsc.PcscPlugin;
import org.keyple.seproxy.ProxyReader;
import org.keyple.seproxy.ReadersPlugin;
import org.keyple.seproxy.SeProxyService;

public class DeportedLogicConsumer {
    public static void main(String[] args) throws Exception {
        SeProxyService seProxyService = SeProxyService.getInstance();
        System.out.println("SeProxyServ v" + seProxyService.getVersion());
        seProxyService.setPlugins(Collections.singletonList(PcscPlugin.getInstance()));
        for (ReadersPlugin rp : seProxyService.getPlugins()) {
            System.out.println("Reader plugin: " + rp.getName());
            for (ProxyReader pr : rp.getReaders()) {
                System.out
                        .println("Reader name: " + pr.getName() + ", present: " + pr.isSEPresent());
                if (pr.isSEPresent()) {
                    BasicCardAccessManager mgr = new BasicCardAccessManager();
                    mgr.setPoReader(pr);
                    mgr.run();
                }
            }
        }
    }
}
