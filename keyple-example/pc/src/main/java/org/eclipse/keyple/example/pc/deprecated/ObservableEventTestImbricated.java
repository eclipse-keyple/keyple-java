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
import org.eclipse.keyple.seproxy.event.AbstractObservableReader;
import org.eclipse.keyple.seproxy.event.AbstractPluginEvent;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.event.ReaderPresencePluginEvent;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.util.Observable;

/**
 * @deprecated following an update in PcscPlugin thread, reader observers should be initially set
 */
public class ObservableEventTestImbricated {
    public static void main(String[] args) throws Exception {
        final Object waitBeforeEnd = new Object();

        PcscPlugin.getInstance().addObserver(new Observable.Observer<AbstractPluginEvent>() {
            @Override
            public void update(Observable observable, AbstractPluginEvent event) {
                if (event instanceof ReaderPresencePluginEvent) {
                    ReaderPresencePluginEvent presence = (ReaderPresencePluginEvent) event;
                    if (presence.isAdded()) {
                        System.out.println("New reader: " + presence.getReader().getName());
                        ProxyReader reader = presence.getReader();
                        if (reader instanceof AbstractObservableReader) {
                            ((AbstractObservableReader) reader)
                                    .addObserver(new Observable.Observer<ReaderEvent>() {
                                        @Override
                                        public void update(Observable observable,
                                                ReaderEvent event) {
                                            if (event.equals(ReaderEvent.SE_INSERTED)) {
                                                System.out.println("Card inserted on: "
                                                        + ((AbstractObservableReader) observable)
                                                                .getName());
                                                analyseCard((AbstractObservableReader) observable);
                                            }
                                        }

                                        private void analyseCard(AbstractObservableReader reader) {
                                            try {
                                                System.out.println(
                                                        "Card present = " + reader.isSePresent());
                                            } catch (IOReaderException ex) {
                                                ex.printStackTrace(System.err);
                                            }
                                        }
                                    });
                        }
                    } else {
                        System.out.println("Removed reader: " + presence.getReader().getName());
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
