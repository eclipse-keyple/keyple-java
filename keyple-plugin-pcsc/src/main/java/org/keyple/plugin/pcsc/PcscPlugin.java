/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.pcsc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import org.keyple.plugin.pcsc.log.CardTerminalsLogger;
import org.keyple.seproxy.ObservableReader;
import org.keyple.seproxy.ReadersPlugin;
import org.keyple.seproxy.exceptions.IOReaderException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public final class PcscPlugin implements ReadersPlugin {

    private static final ILogger logger = SLoggerFactory.getLogger(PcscPlugin.class);

    /**
     * singleton instance of SeProxyService
     */
    private static final PcscPlugin uniqueInstance = new PcscPlugin();

    private static final TerminalFactory factory = TerminalFactory.getDefault();

    private final Map<String, ObservableReader> readers = new HashMap<String, ObservableReader>();

    private boolean logging = false;

    private long waitTimeout = 30000;

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
     */
    public PcscPlugin setLogging(boolean logging) {
        this.logging = logging;
        return this;
    }

    @Override
    public List<ObservableReader> getReaders() throws IOReaderException {
        CardTerminals terminals = getCardTerminals();

        // fclairamb(2018-03-07): This can't happen
        /*
         * if (terminals == null) { logger.error("No terminal found", "action",
         * "pcsc_plugin.no_terminals"); throw new IOReaderException("No terminal found"); }
         */
        try {
            synchronized (readers) {
                for (CardTerminal terminal : terminals.list()) {
                    if (!this.readers.containsKey(terminal.getName())) {
                        PcscReader reader = new PcscReader(terminal);
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
        // fclairamb(2018-02-28): Not a good exception to catch and not a good way to handle it
        /*
         * catch (NullPointerException e) { logger.error("Terminal List not accessible", e); throw
         * new IOReaderException(e.getMessage(), e); }
         */
    }

    private CardTerminals getCardTerminals() {
        CardTerminals terminals = factory.terminals();
        if (logging) {
            terminals = new CardTerminalsLogger(terminals);
        }
        return terminals;
    }

}
