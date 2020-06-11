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
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.plugin.AbstractReader;
import org.eclipse.keyple.core.seproxy.plugin.AbstractThreadedObservablePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class PcscPluginImpl extends AbstractThreadedObservablePlugin implements PcscPlugin {

    private static final Logger logger = LoggerFactory.getLogger(PcscPluginImpl.class);

    private boolean scardNoServiceHackNeeded;

    /**
     * Singleton instance of SeProxyService 'volatile' qualifier ensures that read access to the
     * object will only be allowed once the object has been fully initialized.
     *
     * This qualifier is required for “lazy-singleton” pattern with double-check method, to be
     * thread-safe.
     */
    private static volatile PcscPluginImpl instance; //NOSONAR: We implemented lazy-singleton pattern.

    private PcscPluginImpl() throws KeypleReaderException {
        super(PLUGIN_NAME);
    }

    /**
     * Gets the single instance of PcscPlugin.
     *
     * @return single instance of PcscPlugin
     */
    public static PcscPluginImpl getInstance() throws KeypleReaderException {
        if (instance == null) {
            synchronized (PcscPluginImpl.class) {
                if (instance == null) {
                    instance = new PcscPluginImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public Map<String, String> getParameters() {
        return null;
    }

    @Override
    public void setParameter(String key, String value) {}

    /**
     * Fetch the list of connected native reader (from smartcardio) and returns their names
     *
     * @return connected readers' name list
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    @Override
    public SortedSet<String> fetchNativeReadersNames() throws KeypleReaderIOException {
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
                throw new KeypleReaderIOException("Could not access terminals list", e);
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
    protected SortedSet<SeReader> initNativeReaders() throws KeypleReaderIOException {
        SortedSet<SeReader> nativeReaders = new ConcurrentSkipListSet<SeReader>();

        /*
         * activate a special processing "SCARD_E_NO_NO_SERVICE" (on Windows platforms the removal
         * of the last PC/SC reader stops the "Windows Smart Card service")
         */
        String OS = System.getProperty("os.name").toLowerCase();
        scardNoServiceHackNeeded = OS.indexOf("win") >= 0;
        logger.info("System detected : {}", scardNoServiceHackNeeded);

        // parse the current readers list to create the ProxyReader(s) associated with new reader(s)
        CardTerminals terminals = getCardTerminals();
        logger.trace("[{}] initNativeReaders => CardTerminal in list: {}", this.getName(),
                terminals);
        try {
            for (CardTerminal term : terminals.list()) {
                nativeReaders.add(new PcscReaderImpl(this.getName(), term));
            }
        } catch (CardException e) {
            if (e.getCause().toString().contains("SCARD_E_NO_READERS_AVAILABLE")) {
                logger.trace("No reader available.");
            } else {
                logger.trace("[{}] Terminal list is not accessible. Exception: {}", this.getName(),
                        e.getMessage());
                // throw new KeypleReaderIOException("Could not access terminals list", e); do not
                // propagate exception at the constructor will propagate it as a
                // org.eclipse.keyple.core.seproxy.exception.KeypleRuntimeException

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
     * @throws KeypleReaderNotFoundException if a reader is not found by its name
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    @Override
    protected SeReader fetchNativeReader(String name)
            throws KeypleReaderNotFoundException, KeypleReaderIOException {
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
        try {
            for (CardTerminal term : terminals.list()) {
                if (term.getName().equals(name)) {
                    logger.trace("[{}] fetchNativeReader => CardTerminal in new PcscReader: {}",
                            this.getName(), terminals);
                    reader = new PcscReaderImpl(this.getName(), term);
                }
            }
        } catch (CardException e) {
            logger.trace("[{}] Terminal list is not accessible. Exception: {}", this.getName(),
                    e.getMessage());
            throw new KeypleReaderIOException("Could not access terminals list", e);
        }
        if (reader == null) {
            throw new KeypleReaderNotFoundException("Reader " + name + " not found!");
        }
        return reader;
    }

    private CardTerminals getCardTerminals() {
        if (scardNoServiceHackNeeded) {
            /*
             * This hack avoids the problem of stopping the Windows Smart Card service when removing
             * the last PC/SC reader.
             *
             * Some SONAR warnings have been disabled.
             */
            try {
                Class pcscterminal;
                pcscterminal = Class.forName("sun.security.smartcardio.PCSCTerminals");
                Field contextId = pcscterminal.getDeclaredField("contextId");
                contextId.setAccessible(true); // NOSONAR

                if (contextId.getLong(pcscterminal) != 0L) {
                    Class pcsc = Class.forName("sun.security.smartcardio.PCSC");
                    Method SCardEstablishContext = pcsc.getDeclaredMethod("SCardEstablishContext",
                            new Class[] {Integer.TYPE});
                    SCardEstablishContext.setAccessible(true); // NOSONAR

                    Field SCARD_SCOPE_USER = pcsc.getDeclaredField("SCARD_SCOPE_USER");
                    SCARD_SCOPE_USER.setAccessible(true); // NOSONAR

                    long newId = ((Long) SCardEstablishContext.invoke(pcsc,
                            new Object[] {Integer.valueOf(SCARD_SCOPE_USER.getInt(pcsc))}))
                                    .longValue();
                    contextId.setLong(pcscterminal, newId); // NOSONAR

                    // clear the terminals in cache
                    TerminalFactory factory = TerminalFactory.getDefault();
                    CardTerminals terminals = factory.terminals();
                    Field fieldTerminals = pcscterminal.getDeclaredField("terminals");
                    fieldTerminals.setAccessible(true); // NOSONAR
                    Class classMap = Class.forName("java.util.Map");
                    Method clearMap = classMap.getDeclaredMethod("clear");

                    clearMap.invoke(fieldTerminals.get(terminals));
                }
            } catch (Exception e) {
                logger.error("Unexpected exception.", e);
            }
        }

        return TerminalFactory.getDefault().terminals();
    }
}
