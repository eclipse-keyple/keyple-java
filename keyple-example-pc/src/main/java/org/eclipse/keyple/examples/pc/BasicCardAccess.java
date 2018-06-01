/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.examples.pc;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.example.common.BasicCardAccessManager;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReadersPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.util.Observable;

public class BasicCardAccess {
    private static final Object sync = new Object();

    public static void main(String[] args) throws Exception {
        SeProxyService seProxyService = SeProxyService.getInstance();
        System.out.println("SeProxyServ v" + seProxyService.getVersion());
        List<ReadersPlugin> plugins = new ArrayList<ReadersPlugin>();
        plugins.add(PcscPlugin.getInstance().setLogging(true));
        seProxyService.setPlugins(plugins);
        for (ReadersPlugin rp : seProxyService.getPlugins()) {
            System.out.println("Reader plugin: " + rp.getName());
            for (final ProxyReader pr : rp.getReaders()) {
                System.out
                        .println("Reader name: " + pr.getName() + ", present: " + pr.isSePresent());
                if (pr instanceof AbstractObservableReader) {
                    ((AbstractObservableReader) pr).addObserver(new Observable.Observer<ReaderEvent>() {
                        @Override
                        public void update(Observable observable, ReaderEvent event) {
                            if (event.getEventType() == ReaderEvent.EventType.SE_INSERTED) {
                                parseInfo(pr);
                            }
                        }
                    });
                    /*
                     * ((AbstractObservableReader) pr).addObserver(new Observer() {
                     * 
                     * @Override public void notify(ReaderEvent event) { if (event.getEventType() ==
                     * ReaderEvent.EventType.SE_INSERTED) { parseInfo(pr); } } });
                     */
                } else {
                    parseInfo(pr);
                }
            }
        }

        synchronized (sync) {
            sync.wait();
        }
    }

    private static void parseInfo(ProxyReader poReader) {
        BasicCardAccessManager mgr = new BasicCardAccessManager();
        mgr.setPoReader(poReader);
        mgr.run();
        synchronized (sync) {
            sync.notify();
        }
    }
}
