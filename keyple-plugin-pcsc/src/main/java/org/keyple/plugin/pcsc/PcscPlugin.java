/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.pcsc;

import java.util.*;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

import org.keyple.plugin.pcsc.log.CardTerminalsLogger;
import org.keyple.seproxy.*;
import org.keyple.seproxy.exceptions.IOReaderException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public final class PcscPlugin extends ObservablePlugin {

    private static final ILogger logger = SLoggerFactory.getLogger(PcscPlugin.class);

    private static final long SETTING_THREAD_TIMEOUT_DEFAULT = 30000;

    /**
     * Thread wait timeout in ms
     */
    private long threadWaitTimeout = SETTING_THREAD_TIMEOUT_DEFAULT;

    /**
     * singleton instance of SeProxyService
     */
    private static final PcscPlugin uniqueInstance = new PcscPlugin();

    private static final TerminalFactory factory = TerminalFactory.getDefault();

    private final Map<String, ObservableReader> readers = new HashMap<String, ObservableReader>();

    private boolean logging = false;

    private long waitTimeout = 30000;

    private EventThread thread;

    private PcscPlugin() {
    }

    /**
     * Gets the single instance of PcscPlugin.
     *
     * @return single instance of PcscPlugin
     */
    public static PcscPlugin getInstance() {
        return uniqueInstance;
    }

    @Override
    public String getName() {
        return "PcscPlugin";
    }

    /**
     * Enable the logging
     *
     * @param logging If logging is enabled
     * @return Same instance (fluent setter)
     */
    public PcscPlugin setLogging(boolean logging) {
        this.logging = logging;
        return this;
    }

    @Override
    public List<ObservableReader> getReaders() throws IOReaderException {
        CardTerminals terminals = getCardTerminals();

        try {
            synchronized (readers) {
                for (CardTerminal terminal : terminals.list()) {
                    if (!this.readers.containsKey(terminal.getName())) {
                        PcscReader reader = new PcscReader(terminal);
                        if (logging) {
                            reader.setParameter(PcscReader.SETTING_KEY_LOGGING, "true");
                        }
                        logger.info("New terminal found", "action", "pcsc_plugin.new_terminal",
                                "terminalName", reader.getName());
                        this.readers.put(reader.getName(), reader);
                    }
                }
                return new ArrayList<ObservableReader>(this.readers.values());
            }
        } catch (CardException e) {
            logger.error("Terminal list is not accessible", "action", "pcsc_plugin.no_terminals",
                    "exception", e);
            throw new IOReaderException("Could not access terminals list", e);
        }
    }

    private CardTerminals getCardTerminals() {
        CardTerminals terminals = factory.terminals();
        if (logging) {
            terminals = new CardTerminalsLogger(terminals);
        }
        return terminals;
    }

    @Override
    public final void notifyObservers(PluginEvent event) {
        logger.info("ObservablePlugin: Notifying of an event", "action",
                "observable_plugin.notify_observers", "event", event, "pluginName", getName());
        setChanged();
        super.notifyObservers(event);
    }

    @Override
    public void addObserver(Observer<? super PluginEvent> observer) {
        synchronized (observers) {
            super.addObserver(observer);
            if (observers.size() == 1) {
                if (thread != null) { // <-- This should never happen and can probably be dropped at
                    // some point
                    throw new IllegalStateException("The reader thread shouldn't null");
                }

                thread = new EventThread();
                thread.start();
            }
        }
    }

    @Override
    public void removeObserver(Observer<? super PluginEvent> observer) {
        synchronized (observers) {
            super.removeObserver(observer);
            if (observers.isEmpty()) {
                if (thread == null) { // <-- This should never happen and can probably be dropped at
                    // some point
                    throw new IllegalStateException("The reader thread should be null");
                }

                // We'll let the thread calmly end its course after the waitForCard(Absent|Present)
                // timeout occurs
                thread.end();
                thread = null;
            }
        }
    }

    private void exceptionThrown(Exception e) {
        notifyObservers(new ErrorPluginEvent(e));
    }

    /**
     * Thread in charge of reporting live events
     */
    class EventThread extends Thread {
        private boolean running = true;

        private Map<String, ObservableReader> previousReaders = new HashMap<String, ObservableReader>();

        /**
         * Marks the thread as one that should end when the last cardWaitTimeout occurs
         */
        void end() {
            running = false;
        }

        public void run() {
            try {
                while (running) {
                    Map<String, ObservableReader> previous = new HashMap<String, ObservableReader>(previousReaders);
                    previousReaders = new HashMap<String, ObservableReader>();


                    for (ObservableReader r : getReaders()) {
                        previousReaders.put(r.getName(), r);

                        // If one of the values that are being removed doesn't exist, it means it's a new reader
                        if (previous.remove(r.getName()) == null) {
                            notifyObservers(new ReaderPresencePluginEvent(true, r));
                        }
                    }

                    // If we have a value left that wasn't removed, it means it's a deleted reader
                    for (ObservableReader r : previous.values()) {
                        notifyObservers(new ReaderPresencePluginEvent(false, r));
                    }

                    try {
                        factory.terminals().waitForChange(threadWaitTimeout);
                    }
                    catch(IllegalStateException ex ) {
                        Thread.sleep(5000);
                    }
                }
            } catch (Exception e) {
                exceptionThrown(e);
            }
        }
    }

}
