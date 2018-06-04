/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.pcsc;

import java.util.*;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import org.eclipse.keyple.plugin.pcsc.log.CardTerminalsLogger;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.exceptions.IOReaderException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public final class PcscPlugin extends AbstractObservablePlugin {

    private static final ILogger logger = SLoggerFactory.getLogger(PcscPlugin.class);

    private static final long SETTING_THREAD_TIMEOUT_DEFAULT = 1000;

    /**
     * Thread wait timeout in ms
     */
    private long threadWaitTimeout = SETTING_THREAD_TIMEOUT_DEFAULT;

    /**
     * singleton instance of SeProxyService
     */
    private static final PcscPlugin uniqueInstance = new PcscPlugin();

    private static final TerminalFactory factory = TerminalFactory.getDefault();

    private final Map<String, AbstractObservableReader> readers =
            new HashMap<String, AbstractObservableReader>();

    private boolean logging = false;

    private EventThread thread;

    private PcscPlugin() {}

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
     * @deprecated
     */
    public PcscPlugin setLogging(boolean logging) {
        this.logging = logging;
        return this;
    }

    @Override
    public List<AbstractObservableReader> getReaders() throws IOReaderException {
        CardTerminals terminals = getCardTerminals();

        try {
            // florent(2018-04-15): #64: Fixed the previous logic. It was not removing readers once
            // they disappeared.
            synchronized (readers) {
                Map<String, AbstractObservableReader> previous =
                        new HashMap<String, AbstractObservableReader>(readers);
                for (CardTerminal term : terminals.list()) {
                    if (previous.remove(term.getName()) == null) {
                        PcscReader reader = new PcscReader(term);
                        // TODO reader logging has to be managed with a protected access of the PCSC
                        // plugin package
                        if (logging) {
                            reader.setParameter(PcscReader.SETTING_KEY_LOGGING, "true");
                        }
                        logger.info("New terminal found", "action", "pcsc_plugin.new_terminal",
                                "terminalName", reader.getName());
                        readers.put(reader.getName(), reader);
                    }
                }
                for (Map.Entry<String, AbstractObservableReader> en : previous.entrySet()) {
                    readers.remove(en.getKey());
                }
                return new ArrayList<AbstractObservableReader>(readers.values());
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
    public void addObserver(Observer observer) {
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
    public void removeObserver(Observer observer) {
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
        private boolean initialized = false;

        private Map<String, AbstractObservableReader> previousReaders =
                new HashMap<String, AbstractObservableReader>();

        /**
         * Marks the thread as one that should end when the last cardWaitTimeout occurs
         */
        void end() {
            running = false;
        }

        public void run() {
            try {
                while (running) {
                    Map<String, AbstractObservableReader> previous =
                            new HashMap<String, AbstractObservableReader>(previousReaders);
                    previousReaders = new HashMap<String, AbstractObservableReader>();

                    for (AbstractObservableReader r : getReaders()) {
                        previousReaders.put(r.getName(), r);

                        // If one of the values that are being removed doesn't exist, it means it's
                        // a new reader
                        if (previous.remove(r.getName()) == null && initialized == true) {
                            notifyObservers(new ReaderPresencePluginEvent(true, r));
                        }
                    }

                    // the initial readers list is known, we can now send readers change
                    // notifications
                    initialized = true;

                    // If we have a value left that wasn't removed, it means it's a deleted reader
                    for (AbstractObservableReader r : previous.values()) {
                        notifyObservers(new ReaderPresencePluginEvent(false, r));
                    }

                    try {
                        factory.terminals().waitForChange(threadWaitTimeout);
                    } catch (IllegalStateException ex) {
                        Thread.sleep(1000);
                    }
                }
            } catch (Exception e) {
                exceptionThrown(e);
            }
        }
    }

}
