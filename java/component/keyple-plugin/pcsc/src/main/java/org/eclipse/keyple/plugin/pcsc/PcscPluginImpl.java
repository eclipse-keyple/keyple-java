/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.pcsc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.plugin.AbstractReader;
import org.eclipse.keyple.core.seproxy.plugin.AbstractThreadedObservablePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class PcscPluginImpl extends AbstractThreadedObservablePlugin implements PcscPlugin {

    private static final Logger logger = LoggerFactory.getLogger(PcscPluginImpl.class);

    private static final long SETTING_THREAD_TIMEOUT_DEFAULT = 1000;

    // need to handle executorService because PcscPluginImpl() is called statically
    static ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * singleton instance of SeProxyService
     */
    private static final PcscPluginImpl uniqueInstance = new PcscPluginImpl();

    private static TerminalFactory factory;

    private boolean logging = false;

    private PcscPluginImpl() {
        super(PLUGIN_NAME);
    }

    /**
     * Gets the single instance of PcscPlugin.
     *
     * @return single instance of PcscPlugin
     */
    public static PcscPluginImpl getInstance() {
        return uniqueInstance;
    }

    @Override
    public Map<String, String> getParameters() {
        return null;
    }

    @Override
    public void setParameter(String key, String value) throws IllegalArgumentException {

    }

    /**
     * Enable the logging
     *
     * @param logging If logging is enabled
     * @return Same instance (fluent setter)
     * @deprecated
     */
    public PcscPluginImpl setLogging(boolean logging) {
        this.logging = logging;
        return this;
    }

    /**
     * Fetch the list of connected native reader (from smartcardio) and returns their names
     *
     * @return connected readers' name list
     * @throws KeypleReaderException if a reader error occurs
     */
    public SortedSet<String> fetchNativeReadersNames() throws KeypleReaderException {
        SortedSet<String> nativeReadersNames = new ConcurrentSkipListSet<String>();
        CardTerminals terminals = getCardTerminals();
        try {
            for (CardTerminal term : terminals.list()) {
                nativeReadersNames.add(term.getName());
            }
        } catch (CardException e) {
            if (e.getCause().toString().contains("SCARD_E_NO_READERS_AVAILABLE")) {
                logger.trace("No reader available.");
            } else {
                logger.trace(
                        "[{}] fetchNativeReadersNames => Terminal list is not accessible. Exception: {}",
                        this.getName(), e.getMessage());
                throw new KeypleReaderException("Could not access terminals list", e);
            }
        }
        return nativeReadersNames;
    }

    /**
     * Fetch connected native readers (from smartcard.io) and returns a list of corresponding
     * {@link AbstractReader} {@link AbstractReader} are new instances.
     *
     * @return the list of AbstractReader objects.
     * @throws KeypleReaderException if a reader error occurs
     */
    @Override
    protected SortedSet<SeReader> initNativeReaders() throws KeypleReaderException {
        SortedSet<SeReader> nativeReaders = new ConcurrentSkipListSet<SeReader>();

        // parse the current readers list to create the ProxyReader(s) associated with new reader(s)
        CardTerminals terminals = getCardTerminals();
        logger.trace("[{}] initNativeReaders => CardTerminal in list: {}", this.getName(),
                terminals);
        try {
            for (CardTerminal term : terminals.list()) {

                nativeReaders.add(new PcscReaderImpl(this.getName(), term, executorService));
            }
        } catch (CardException e) {
            if (e.getCause().toString().contains("SCARD_E_NO_READERS_AVAILABLE")) {
                logger.trace("No reader available.");
            } else {
                logger.trace("[{}] Terminal list is not accessible. Exception: {}", this.getName(),
                        e.getMessage());
                throw new KeypleReaderException("Could not access terminals list", e);

            }
        }
        return nativeReaders;
    }

    /**
     * Fetch the reader whose name is provided as an argument. Returns the current reader if it is
     * already listed. Creates and returns a new reader if not.
     *
     * Throws an exception if the wanted reader is not found.
     *
     * @param name name of the reader
     * @return the reader object
     * @throws KeypleReaderException if a reader error occurs
     */
    @Override
    protected SeReader fetchNativeReader(String name) throws KeypleReaderException {
        // return the current reader if it is already listed
        for (SeReader reader : readers) {
            if (reader.getName().equals(name)) {
                return reader;
            }
        }
        /*
         * parse the current PC/SC readers list to create the ProxyReader(s) associated with new
         * reader(s)
         */
        AbstractReader reader = null;
        CardTerminals terminals = getCardTerminals();
        List<String> terminalList = new ArrayList<String>();
        try {
            for (CardTerminal term : terminals.list()) {
                if (term.getName().equals(name)) {
                    logger.trace("[{}] fetchNativeReader => CardTerminal in new PcscReader: {}",
                            this.getName(), terminals);
                    reader = new PcscReaderImpl(this.getName(), term, executorService);
                }
            }
        } catch (CardException e) {
            logger.trace("[{}] Terminal list is not accessible. Exception: {}", this.getName(),
                    e.getMessage());
            throw new KeypleReaderException("Could not access terminals list", e);
        }
        if (reader == null) {
            throw new KeypleReaderException("Reader " + name + " not found!");
        }
        return reader;
    }

    private CardTerminals getCardTerminals() {
        try {
            Class pcscterminal = null;
            pcscterminal = Class.forName("sun.security.smartcardio.PCSCTerminals");
            Field contextId = pcscterminal.getDeclaredField("contextId");
            contextId.setAccessible(true);

            if (contextId.getLong(pcscterminal) != 0L) {
                Class pcsc = Class.forName("sun.security.smartcardio.PCSC");
                Method SCardEstablishContext =
                        pcsc.getDeclaredMethod("SCardEstablishContext", new Class[] {Integer.TYPE});
                SCardEstablishContext.setAccessible(true);

                Field SCARD_SCOPE_USER = pcsc.getDeclaredField("SCARD_SCOPE_USER");
                SCARD_SCOPE_USER.setAccessible(true);

                long newId = ((Long) SCardEstablishContext.invoke(pcsc,
                        new Object[] {Integer.valueOf(SCARD_SCOPE_USER.getInt(pcsc))})).longValue();
                contextId.setLong(pcscterminal, newId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (factory == null) {
            factory = TerminalFactory.getDefault();
        }

        CardTerminals terminals = factory.terminals();

        return terminals;
    }
}
