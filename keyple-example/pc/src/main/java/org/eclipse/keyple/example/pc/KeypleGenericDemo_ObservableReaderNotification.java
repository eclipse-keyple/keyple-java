/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.pc;

import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.*;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.exception.UnexpectedPluginException;


public class KeypleGenericDemo_ObservableReaderNotification {

    private SpecificReaderObserver readerObserver;
    private SpecificPluginObserver pluginObserver;

    private KeypleGenericDemo_ObservableReaderNotification() {
        readerObserver = new SpecificReaderObserver();
        pluginObserver = new SpecificPluginObserver(readerObserver);
    }

    private static void listReaders() throws IOReaderException {

        int pluginIndex = 0;
        for (ReadersPlugin plugin : SeProxyService.getInstance().getPlugins()) {
            pluginIndex++;
            int readerIndex = 0;
            for (ProxyReader reader : plugin.getReaders()) {
                try {
                    System.out.println(pluginIndex + "\t" + plugin.getName() + "\t" + readerIndex++
                            + "\t" + reader.getName() + "\t"
                            + ((reader.isSePresent()) ? "card_present" : "card_absent") + "\t");
                } catch (NoStackTraceThrowable noStackTraceThrowable) {
                    noStackTraceThrowable.printStackTrace();
                }
            }
        }
    }

    private void setObservers() throws IOReaderException {

        for (ReadersPlugin plugin : SeProxyService.getInstance().getPlugins()) {

            if (plugin instanceof ObservablePlugin) {
                System.out.println("Add observer on the plugin :  " + plugin.getName());
                ((ObservablePlugin) plugin).addObserver(this.pluginObserver);
            } else {
                System.out.println("Plugin " + plugin.getName() + " isn't observable");
            }

            for (ProxyReader reader : plugin.getReaders()) {
                if (reader instanceof ObservableReader) {
                    System.out.println("Add observer on the reader :  " + reader.getName());
                    ((ObservableReader) reader).addObserver(this.readerObserver);
                } else {
                    System.out.println("Reader " + reader.getName() + " isn't observable");
                }
            }
        }
    }

    public class SpecificReaderObserver implements ObservableReader.ReaderObserver {

        SpecificReaderObserver() {
            super();
        }

        public void update(ReaderEvent event) {
            switch (event.getEventType()) {
                case SE_INSERTED:
                    System.out.println("Plugin: " + event.getPluginName() + ", card inserted on: "
                            + event.getReaderName());
                    // analyseCard((AbstractObservableReader) reader);
                    break;
                case SE_REMOVAL:
                    System.out.println("Plugin: " + event.getPluginName() + ", card removed on: "
                            + event.getReaderName());
                    break;
                default:
                    System.out.println("Plugin: " + event.getPluginName()
                            + ", unexpected event on: " + event.getReaderName() + ", event: "
                            + event.getEventType().getName());
                    break;
            }
            try {
                listReaders();
            } catch (IOReaderException e) {
                e.printStackTrace();
            }
        }

        private void analyseCard(ObservableReader reader) {
            try {
                System.out.println("Card present = " + reader.isSePresent());
            } catch (NoStackTraceThrowable ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    public class SpecificPluginObserver implements ObservablePlugin.PluginObserver {

        SpecificReaderObserver readerObserver;

        SpecificPluginObserver(SpecificReaderObserver readerObserver) {
            this.readerObserver = readerObserver;
        }

        @Override
        public void update(PluginEvent event) {
            ProxyReader reader = null;

            switch (event.getEventType()) {
                case READER_CONNECTED:
                    System.out.println("New reader: " + reader.getName());

                    if (reader instanceof ObservableReader) {

                        if (readerObserver != null) {
                            ((ObservableReader) reader).addObserver(readerObserver);
                            System.out.println(
                                    "Add observer on the plugged reader :  " + reader.getName());
                        } else {
                            System.out.println("No observer to add to the plugged reader :  "
                                    + reader.getName());
                        }
                    }
                    break;
                case READER_DISCONNECTED:
                    System.out.println("Reader removed: " + event.getReaderName());

                    if (reader instanceof ObservableReader) {

                        if (readerObserver != null) {
                            ((ObservableReader) reader).removeObserver(readerObserver);
                            System.out.println("Remove observer on the unplugged reader :  "
                                    + event.getReaderName());
                        } else {
                            System.out.println("Unplugged reader " + event.getReaderName()
                                    + " wasn't observed");
                        }
                    }
                    break;
                default:
                    System.out
                            .println("Unexpected reader event: " + event.getEventType().getName());
                    break;
            }

            try {
                listReaders();
                if (SeProxyService.getInstance().getPlugin(event.getPluginName()).getReaders()
                        .isEmpty()) {
                    System.out.println("EXIT - no more reader");
                    synchronized (waitBeforeEnd) {
                        waitBeforeEnd.notify();
                    }
                }
            } catch (IOReaderException e) {
                e.printStackTrace();
            } catch (UnexpectedPluginException e) {
                e.printStackTrace();
            }
        }
    }

    private final static Object waitBeforeEnd = new Object();

    public static void main(String[] args) throws Exception {
        KeypleGenericDemo_ObservableReaderNotification testObserver =
                new KeypleGenericDemo_ObservableReaderNotification();

        // Set PCSC plugin
        SeProxyService seProxyService = SeProxyService.getInstance();
        SortedSet<ReadersPlugin> pluginsSet = new ConcurrentSkipListSet<ReadersPlugin>();
        pluginsSet.add(PcscPlugin.getInstance().setLogging(false));
        seProxyService.setPlugins(pluginsSet);

        // Print reader configuration
        listReaders();

        // Set observer
        testObserver.setObservers();

        // Print reader configuration
        listReaders();

        // the program will stop when the last connected reader is unplugged
        synchronized (waitBeforeEnd) {
            waitBeforeEnd.wait();
        }
    }
}
