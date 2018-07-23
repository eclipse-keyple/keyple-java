/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy.plugin;


import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;


/**
 * 
 * Abstract definition of an observable reader. Factorizes setSetProtocols and will factorize the
 * transmit method logging
 * 
 */

public abstract class AbstractObservableReader extends AbstractLoggedObservable<ReaderEvent>
        implements ProxyReader {
    // TODO check for a better way to log
    private static final ILogger logger = SLoggerFactory.getLogger(AbstractObservableReader.class);

    private final String pluginName;

    protected abstract SeResponseSet processSeRequestSet(SeRequestSet requestSet)
            throws IOReaderException;

    /**
     * Reader constructor<br/>
     * Force the definition of a name through the use of super method.
     *
     * @param pluginName the name of the plugin that instantiated the reader
     * @param readerName the name of the reader
     */
    protected AbstractObservableReader(String pluginName, String readerName) {
        super(readerName);
        this.pluginName = pluginName;
    }

    /**
     * Implementation must call logSeRequestSet before transmit and logSeResponseSet after transmit
     *
     * @param requestSet
     * @return responseSet
     * @throws IOReaderException
     */
    public final SeResponseSet transmit(SeRequestSet requestSet) throws IOReaderException {
        assert requestSet !=null : "Se Request Set must not be null";

        // TODO do a better log of SeRequestSet data
        logger.info("SeRequestSet", "reader", this.getName(), "data", requestSet.toString());

        long before = System.nanoTime();
        SeResponseSet responseSet;

        try {
            responseSet = processSeRequestSet(requestSet);
        } catch (IOReaderException ex) {
            // Switching to the 10th of milliseconds and dividing by 10 to get the ms
            double elapsedMs = (double) ((System.nanoTime() - before) / 100000) / 10;
            logger.info("LocalReader: Data exchange", "action", "local_reader.transmit_failure",
                    "requestSet", requestSet, "elapsedMs", elapsedMs);
            throw ex;
        }

        // Switching to the 10th of milliseconds and dividing by 10 to get the ms
        double elapsedMs = (double) ((System.nanoTime() - before) / 100000) / 10;
        logger.info("LocalReader: Data exchange", "action", "local_reader.transmit", "reader",
                this.getName(), "requestSet", requestSet, "responseSet", responseSet, "elapsedMs",
                elapsedMs);

        // TODO do a better log of SeReponseSet data
        logger.info("SeResponseSet", "reader", this.getName(), "data", responseSet.toString());

        return responseSet;
    }

    /**
     * @return Plugin name
     */
    protected final String getPluginName() {
        return pluginName;
    }

    /**
     * Compare the name of the current ProxyReader to the name of the ProxyReader provided in
     * argument
     * 
     * @param proxyReader
     * @return true if the names match (The method is needed for the SortedSet lists)
     */
    public final int compareTo(ProxyReader proxyReader) {
        return this.getName().compareTo(proxyReader.getName());
    }
}
