/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.pcsc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import org.eclipse.keyple.plugin.pcsc.log.CardTerminalsLogger;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.plugin.AbstractObservableReader;
import org.eclipse.keyple.seproxy.plugin.AbstractThreadedObservablePlugin;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public final class PcscPlugin extends AbstractThreadedObservablePlugin {

    private static final ILogger logger = SLoggerFactory.getLogger(PcscPlugin.class);

    private static final long SETTING_THREAD_TIMEOUT_DEFAULT = 1000;

    /**
     * singleton instance of SeProxyService
     */
    private static final PcscPlugin uniqueInstance = new PcscPlugin();

    private static TerminalFactory factory;


    private boolean logging = false;

    private PcscPlugin() {
        super("PcscPlugin");
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
    public Map<String, String> getParameters() {
        return null;
    }

    @Override
    public void setParameter(String key, String value) throws IOException {

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

    protected SortedSet<String> getNativeReadersNames() throws IOReaderException {
        SortedSet<String> nativeReadersNames = new ConcurrentSkipListSet<String>();
        CardTerminals terminals = getCardTerminals();
        try {
            for (CardTerminal term : terminals.list()) {
                nativeReadersNames.add(term.getName());
            }
        } catch (CardException e) {
            logger.error("Terminal list is not accessible", "action", "pcsc_plugin.no_terminals",
                    "exception", e);
            throw new IOReaderException("Could not access terminals list", e);
        }
        return nativeReadersNames;
    }

    /**
     * Gets the list of all native readers
     *
     * New reader objects are created.
     * 
     * @return the list of new readers.
     * @throws IOReaderException if a reader error occurs
     */
    @Override
    protected SortedSet<AbstractObservableReader> getNativeReaders() throws IOReaderException {
        SortedSet<AbstractObservableReader> nativeReaders =
                new ConcurrentSkipListSet<AbstractObservableReader>();

        // parse the current readers list to create the ProxyReader(s) associated with new reader(s)
        CardTerminals terminals = getCardTerminals();
        try {
            for (CardTerminal term : terminals.list()) {
                nativeReaders.add(new PcscReader(this.getName(), term));
            }
        } catch (CardException e) {
            logger.error("Terminal list is not accessible", "action", "pcsc_plugin.no_terminals",
                    "exception", e);
            throw new IOReaderException("Could not access terminals list", e);
        }
        return nativeReaders;
    }

    /**
     * Gets the reader whose name is provided as an argument.
     *
     * Returns the current reader if it is already listed.
     *
     * Creates and returns a new reader if not.
     *
     * Throws an exception if the wanted reader is not found.
     * 
     * @param name name of the reader
     * @return the reader object
     * @throws IOReaderException if a reader error occurs
     */
    @Override
    protected AbstractObservableReader getNativeReader(String name) throws IOReaderException {
        // return the current reader if it is already listed
        for (AbstractObservableReader reader : readers) {
            if (reader.getName().equals(name)) {
                return reader;
            }
        }
        // parse the current PC/SC readers list to create the ProxyReader(s) associated with new
        // reader(s)
        AbstractObservableReader reader = null;
        CardTerminals terminals = getCardTerminals();
        List<String> terminalList = new ArrayList<String>();
        try {
            for (CardTerminal term : terminals.list()) {
                if (term.getName().equals(name)) {
                    reader = new PcscReader(this.getName(), term);
                }
            }
        } catch (CardException e) {
            logger.error("Terminal list is not accessible", "action", "pcsc_plugin.no_terminals",
                    "exception", e);
            throw new IOReaderException("Could not access terminals list", e);
        }
        if (reader == null) {
            throw new IOReaderException("Reader " + name + " not found!");
        }
        return reader;
    }

    private CardTerminals getCardTerminals() {
        if (factory == null) {
            factory = TerminalFactory.getDefault();
        }
        CardTerminals terminals = factory.terminals();
        if (logging) {
            terminals = new CardTerminalsLogger(terminals);
        }
        return terminals;
    }
}
