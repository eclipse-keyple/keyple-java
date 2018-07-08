/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.pc.deprecated;

import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.event.AbstractPluginEvent;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.event.ReaderPresencePluginEvent;
import org.eclipse.keyple.seproxy.exception.UnexpectedPluginException;
import org.eclipse.keyple.seproxy.exception.UnexpectedReaderException;
import org.eclipse.keyple.seproxy.plugin.AbstractObservableReader;
import org.eclipse.keyple.util.Observable;

/**
 * @deprecated following an update in PcscPlugin thread, reader observers should be initially set
 */
public class ObservableEventTestImbricated {
    public static void main(String[] args) throws Exception {
        final Object waitBeforeEnd = new Object();

        PcscPlugin.getInstance().addObserver(new Observable.Observer<AbstractPluginEvent>() {
            @Override
            public void update(AbstractPluginEvent event) {
                if (event instanceof ReaderPresencePluginEvent) {
                    ReaderPresencePluginEvent presence = (ReaderPresencePluginEvent) event;
                    if (presence.isAdded()) {
                        ProxyReader reader = null;
                        try {
                            reader = SeProxyService.getInstance()
                                    .getPlugin(presence.getPluginName())
                                    .getReader(presence.getReaderName());
                        } catch (UnexpectedReaderException e) {
                            e.printStackTrace();
                        } catch (UnexpectedPluginException e) {
                            e.printStackTrace();
                        }
                        System.out.println("New reader: " + reader.getName());
                        if (reader instanceof AbstractObservableReader) {
                            ((AbstractObservableReader) reader)
                                    .addObserver(new Observable.Observer<ReaderEvent>() {
                                        @Override
                                        public void update(ReaderEvent event) {
                                            if (event.getEventType().equals(ReaderEvent.EventType.SE_INSERTED)) {
                                                System.out.println(
                                                        "Card inserted on: " + event.getReaderName());
                                            }
                                        }
                                    });
                        }
                    } else {
                        System.out.println("Removed reader: " + presence.getReaderName());
                        synchronized (waitBeforeEnd) {
                            waitBeforeEnd.notify();
                        }
                    }
                }
            }
        });

        synchronized (waitBeforeEnd) {
            waitBeforeEnd.wait();
        }
    }
}
