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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.keyple.plugin.pcsc.log.CardTerminalsLogger;
import org.keyple.seproxy.ObservableReader;
import org.keyple.seproxy.ReadersPlugin;
import org.keyple.seproxy.exceptions.IOReaderException;

public final class PcscPlugin implements ReadersPlugin {

    /**
     * singleton instance of SeProxyService
     */
    private static final PcscPlugin uniqueInstance = new PcscPlugin();

    private static final Logger logger = LogManager.getLogger(PcscPlugin.class);

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

        if (terminals == null) {
            logger.error("No terminal found");
            throw new IOReaderException("No terminal found");
        }
        try {
            if (this.readers.isEmpty()) {
                for (CardTerminal terminal : terminals.list()) {
                    PcscReader reader = new PcscReader(terminal, terminal.getName());
                    if (!this.readers.containsKey(reader.getName())) {
                        this.readers.put(reader.getName(), reader);
                    }
                }
            }
        } catch (CardException e) {
            logger.error("Terminal List not accessible", e);
            throw new IOReaderException(e);
        }
        // fclairamb(2018-02-28): Not a good exception to catch and not a good way to handle it
        /*
         * catch (NullPointerException e) { logger.error("Terminal List not accessible", e); throw
         * new IOReaderException(e.getMessage(), e); }
         */

        return new ArrayList<ObservableReader>(this.readers.values());
    }

    private CardTerminals getCardTerminals() {
        CardTerminals terminals = factory.terminals();
        if (logging) {
            terminals = new CardTerminalsLogger(terminals);
        }
        return terminals;
    }

}
