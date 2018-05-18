/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.examples.pc;

import java.util.ArrayList;
import java.util.List;
import org.keyple.plugin.pcsc.PcscPlugin;
import org.keyple.seproxy.*;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.keyple.util.Observable;


public class ObservableEventTest {

    private ReaderObserver readerObserver;
    private PluginObserver pluginObserver;

    private ObservableEventTest() {
        readerObserver = new ReaderObserver();
        pluginObserver = new PluginObserver(readerObserver);
    }

    private static void listReaders() throws IOReaderException {

        int pluginIndex = 0;
        for (ReadersPlugin plugin : SeProxyService.getInstance().getPlugins()) {
            pluginIndex++;
            int readerIndex = 0;
            for (ProxyReader reader : plugin.getReaders()) {
                System.out.println(pluginIndex + "\t" + plugin.getName() + "\t" + readerIndex++
                        + "\t" + reader.getName() + "\t"
                        + ((reader.isSEPresent()) ? "card_present" : "card_absent") + "\t"
                        + ((((AbstractObservableReader) reader).countObservers() > 0)
                                ? "observed_reader"
                                : "not_observed_reader"));
            }
        }
    }

    private void setObservers() throws IOReaderException {

        for (ReadersPlugin plugin : SeProxyService.getInstance().getPlugins()) {

            if (plugin instanceof AbstractObservablePlugin) {
                System.out.println("Add observer on the plugin :  " + plugin.getName());
                ((AbstractObservablePlugin) plugin).addObserver(this.pluginObserver);
            } else {
                System.out.println("Plugin " + plugin.getName() + " isn't observable");
            }

            for (ProxyReader reader : plugin.getReaders()) {
                if (reader instanceof AbstractObservableReader) {
                    System.out.println("Add observer on the reader :  " + reader.getName());
                    ((AbstractObservableReader) reader).addObserver(this.readerObserver);
                } else {
                    System.out.println("Reader " + reader.getName() + " isn't observable");
                }
            }
        }
    }

    public class ReaderObserver implements AbstractObservableReader.Observer<ReaderEvent> {

        ReaderObserver() {
            super();
        }

        public void update(Observable<? extends ReaderEvent> observable, ReaderEvent event) {
            if (event.getEventType().equals(ReaderEvent.EventType.SE_INSERTED)) {
                System.out.println("Card inserted on: " + event.getReader().getName());
                analyseCard(event.getReader());
            } else if (event.getEventType().equals(ReaderEvent.EventType.SE_REMOVAL)) {
                System.out.println("Card removed on: " + event.getReader().getName());
            }
            try {
                listReaders();
            } catch (IOReaderException e) {
                e.printStackTrace();
            }
        }

        private void analyseCard(AbstractObservableReader reader) {
            try {
                System.out.println("Card present = " + reader.isSEPresent());
            } catch (IOReaderException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    public class PluginObserver implements AbstractObservablePlugin.Observer<PluginEvent> {

        ReaderObserver readerObserver;

        PluginObserver(ReaderObserver readerObserver) {
            this.readerObserver = readerObserver;
        }

        @Override
        public void update(Observable<? extends PluginEvent> observable, PluginEvent event) {
            if (event instanceof ReaderPresencePluginEvent) {
                ReaderPresencePluginEvent presence = (ReaderPresencePluginEvent) event;
                ProxyReader reader = presence.getReader();
                if (presence.isAdded()) {
                    System.out.println("New reader: " + reader.getName());

                    if (reader instanceof AbstractObservableReader) {

                        if (readerObserver != null) {
                            ((AbstractObservableReader) reader).addObserver(readerObserver);
                            System.out.println(
                                    "Add observer on the plugged reader :  " + reader.getName());
                        } else {
                            System.out.println("No observer to add to the plugged reader :  "
                                    + reader.getName());
                        }
                    }
                } else {
                    System.out.println("Reader removed: " + reader.getName());

                    if (reader instanceof AbstractObservableReader) {

                        if (readerObserver != null) {
                            ((AbstractObservableReader) reader).removeObserver(readerObserver);
                            System.out.println("Remove observer on the unplugged reader :  "
                                    + reader.getName());
                        } else {
                            System.out.println(
                                    "Unplugged reader " + reader.getName() + " wasn't observed");
                        }
                    }
                }

                try {
                    listReaders();

                    if (((AbstractObservablePlugin) observable).getReaders().isEmpty()) {
                        System.out.println("EXIT - no more reader");
                        synchronized (waitBeforeEnd) {
                            waitBeforeEnd.notify();
                        }
                    }
                } catch (IOReaderException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private final static Object waitBeforeEnd = new Object();

    public static void main(String[] args) throws Exception {

        ObservableEventTest testObserver = new ObservableEventTest();

        // Set PCSC plugin
        SeProxyService seProxyService = SeProxyService.getInstance();
        List<ReadersPlugin> pluginsSet = new ArrayList<ReadersPlugin>();
        pluginsSet.add(PcscPlugin.getInstance().setLogging(false));
        seProxyService.setPlugins(pluginsSet);

        // Print reader configuration
        listReaders();

        // Set observer
        testObserver.setObservers();

        // Print reader configuration
        listReaders();

        synchronized (waitBeforeEnd) {
            waitBeforeEnd.wait();
        }
    }
}
