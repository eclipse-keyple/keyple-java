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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * Abstract definition of an observable reader. Factorizes setSetProtocols and will factorize the
 * transmit method logging
 * 
 */

public abstract class AbstractObservableReader extends AbstractLoggedObservable<ReaderEvent>
        implements ProxyReader {

    private static final Logger logger = LoggerFactory.getLogger(AbstractObservableReader.class);

    private final String pluginName;

    protected abstract SeResponseSet processSeRequestSet(SeRequestSet requestSet)
            throws IOReaderException;

    /**
     * Reader constructor
     *
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
     * @param requestSet the request set
     * @return responseSet the response set
     * @throws IOReaderException if a reader error occurs
     */
    public final SeResponseSet transmit(SeRequestSet requestSet) throws IOReaderException {
        if (requestSet == null) {
            throw new IOReaderException("seRequestSet must not be null");
        }

        SeResponseSet responseSet;
        long before = 0;


        if(logger.isDebugEnabled()) {
            logger.trace("[{}] transmit => SeRequestSet: {}", this.getName(), requestSet.toString());
            before = System.nanoTime();
        }

        try {
            responseSet = processSeRequestSet(requestSet);
        } catch (IOReaderException ex) {
            // Switching to the 10th of milliseconds and dividing by 10 to get the ms
            double elapsedMs = (double) ((System.nanoTime() - before) / 100000) / 10;
            logger.trace("[{}] transmit => failure. elapsed {}", elapsedMs);
            throw ex;
        }

        if(logger.isDebugEnabled()) {
            // Switching to the 10th of milliseconds and dividing by 10 to get the ms
            double elapsedMs = (double) ((System.nanoTime() - before) / 100000) / 10;
            logger.trace("[{}] transmit => SeResponse: {}, elapsed {} ms.", this.getName(),
                    responseSet.toString(), elapsedMs);
        }

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
     * @param proxyReader a ProxyReader object
     * @return true if the names match (The method is needed for the SortedSet lists)
     */
    public final int compareTo(ProxyReader proxyReader) {
        return this.getName().compareTo(proxyReader.getName());
    }
}
