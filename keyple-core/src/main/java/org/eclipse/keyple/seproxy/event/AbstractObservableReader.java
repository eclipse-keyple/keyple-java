/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy.event;


import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.plugin.AbstractLocalReader;
import org.eclipse.keyple.seproxy.plugin.AbstractLoggedObservable;
import org.eclipse.keyple.util.Observable;
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
    private static final ILogger logger = SLoggerFactory.getLogger(AbstractLocalReader.class);

    public interface ReaderObserver extends AbstractLoggedObservable.Observer<ReaderEvent> {
        void update(Observable reader, ReaderEvent event);
    }

    public abstract SeResponseSet processSeRequestSet(SeRequestSet requestSet)
            throws IOReaderException;

    /**
     * Implementation must call logSeRequestSet before transmit and logSeResponseSet after transmit
     * 
     * @param requestSet
     * @return responseSet
     * @throws IOReaderException
     */
    public final SeResponseSet transmit(SeRequestSet requestSet) throws IOReaderException {
        logSeRequestSet(requestSet);
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
        logger.info("LocalReader: Data exchange", "action", "local_reader.transmit", "requestSet",
                requestSet, "responseSet", responseSet, "elapsedMs", elapsedMs);

        logSeResponseSet(responseSet);

        return responseSet;
    }

    private void logSeRequestSet(SeRequestSet requestSet) {
        // TODO do a better log of SeRequestSet data
        logger.info("SeRequestSet", "data", requestSet.toString());
    }

    private void logSeResponseSet(SeResponseSet responseSet) {
        // TODO do a better log of SeReponseSet data
        logger.info("SeResponseSet", "data", responseSet.toString());
    }
}
