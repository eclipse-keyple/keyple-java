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
package org.eclipse.keyple.seproxy.plugin;


import org.eclipse.keyple.seproxy.SeReader;
import org.eclipse.keyple.seproxy.event.DefaultSelectionRequest;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleChannelStateException;
import org.eclipse.keyple.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.message.ProxyReader;
import org.eclipse.keyple.seproxy.message.SeRequest;
import org.eclipse.keyple.seproxy.message.SeRequestSet;
import org.eclipse.keyple.seproxy.message.SeResponse;
import org.eclipse.keyple.seproxy.message.SeResponseSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract definition of an observable reader.
 * <ul>
 * <li>High level logging and benchmarking of SeRequestSet and SeRequest transmission</li>
 * <li>Observability management</li>
 * <li>Name-based comparison of ProxyReader (required for SortedSet&lt;ProxyReader&gt;)</li>
 * <li>Plugin naming management</li>
 * </ul>
 */

public abstract class AbstractObservableReader extends AbstractLoggedObservable<ReaderEvent>
        implements ObservableReader, ProxyReader {

    /** logger */
    private static final Logger logger = LoggerFactory.getLogger(AbstractObservableReader.class);

    /** Timestamp recorder */
    private long before;

    /** Contains the name of the plugin */
    protected final String pluginName;

    /** The default DefaultSelectionRequest to be executed upon SE insertion */
    protected DefaultSelectionRequest defaultSelectionRequest;

    /** Indicate if all SE detected should be notified or only matching SE */
    protected ObservableReader.NotificationMode notificationMode;

    /** ==== Constructor =================================================== */

    /**
     * Reader constructor
     * <p>
     * Force the definition of a name through the use of super method.
     * <p>
     * Initialize the time measurement
     *
     * @param pluginName the name of the plugin that instantiated the reader
     * @param readerName the name of the reader
     */
    protected AbstractObservableReader(String pluginName, String readerName) {
        super(readerName);
        this.pluginName = pluginName;
        this.before = System.nanoTime(); /*
                                          * provides an initial value for measuring the
                                          * inter-exchange time. The first measurement gives the
                                          * time elapsed since the plugin was loaded.
                                          */
    }

    /** ==== Utility methods =============================================== */

    /**
     * Gets the name of plugin provided in the constructor.
     * <p>
     * The method will be used particularly for logging purposes. The plugin name is also part of
     * the ReaderEvent and PluginEvent objects.
     * 
     * @return the plugin name String
     */
    protected final String getPluginName() {
        return pluginName;
    }

    /**
     * Compare the name of the current SeReader to the name of the SeReader provided in argument
     *
     * @param seReader a SeReader object
     * @return true if the names match (The method is needed for the SortedSet lists)
     */
    public final int compareTo(SeReader seReader) {
        return this.getName().compareTo(seReader.getName());
    }

    /** ==== High level communication API ================================== */

    /**
     * Execute the transmission of a list of {@link SeRequest} and returns a list of
     * {@link SeResponse}
     * <p>
     * The global execution time (inter-exchange and communication) and the SeRequestSet content is
     * logged (DEBUG level).
     * <p>
     * As the method is final, it cannot be extended.
     *
     * @param requestSet the request set
     * @return responseSet the response set
     * @throws KeypleReaderException if a reader error occurs
     */
    public final SeResponseSet transmitSet(SeRequestSet requestSet) throws KeypleReaderException {
        if (requestSet == null) {
            throw new IllegalArgumentException("seRequestSet must not be null");
        }

        SeResponseSet responseSet;

        if (logger.isDebugEnabled()) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.debug("[{}] transmit => SEREQUESTSET = {}, elapsed {} ms.", this.getName(),
                    requestSet.toString(), elapsedMs);
        }

        try {
            responseSet = processSeRequestSet(requestSet);
        } catch (KeypleChannelStateException ex) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.debug("[{}] transmit => SEREQUESTSET channel failure. elapsed {}", elapsedMs);
            /* Throw an exception with the responses collected so far. */
            throw ex;
        } catch (KeypleIOReaderException ex) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.debug("[{}] transmit => SEREQUESTSET IO failure. elapsed {}", elapsedMs);
            /* Throw an exception with the responses collected so far. */
            throw ex;
        }

        if (logger.isDebugEnabled()) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - before) / 100000) / 10;
            this.before = timeStamp;
            logger.debug("[{}] transmit => SERESPONSESET = {}, elapsed {} ms.", this.getName(),
                    responseSet.toString(), elapsedMs);
        }

        return responseSet;
    }

    /**
     * Abstract method implemented by the AbstractLocalReader and VirtualReader classes.
     * <p>
     * This method is handled by transmitSet.
     * 
     * @param requestSet the SeRequestSet to be processed
     * @return the SeResponseSet (responses to the SeRequestSet)
     * @throws KeypleReaderException if reader error occurs
     */
    protected abstract SeResponseSet processSeRequestSet(SeRequestSet requestSet)
            throws KeypleReaderException;

    /**
     * Execute the transmission of a {@link SeRequest} and returns a {@link SeResponse}
     * <p>
     * The individual execution time (inter-exchange and communication) and the SeRequestSet content
     * is logged (DEBUG level).
     * <p>
     * As the method is final, it cannot be extended.
     *
     * @param seRequest the request to be transmitted
     * @return the received response
     * @throws KeypleReaderException if a reader error occurs
     */
    public final SeResponse transmit(SeRequest seRequest) throws KeypleReaderException {
        if (seRequest == null) {
            throw new IllegalArgumentException("seRequest must not be null");
        }

        SeResponse seResponse = null;

        if (logger.isDebugEnabled()) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.debug("[{}] transmit => SEREQUEST = {}, elapsed {} ms.", this.getName(),
                    seRequest.toString(), elapsedMs);
        }

        try {
            seResponse = processSeRequest(seRequest);
        } catch (KeypleChannelStateException ex) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.debug("[{}] transmit => SEREQUEST channel failure. elapsed {}", this.getName(),
                    elapsedMs);
            /* Throw an exception with the responses collected so far (ex.getSeResponse()). */
            throw ex;
        } catch (KeypleIOReaderException ex) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.debug("[{}] transmit => SEREQUEST IO failure. elapsed {}", this.getName(),
                    elapsedMs);
            /* Throw an exception with the responses collected so far (ex.getSeResponse()). */
            throw ex;
        }

        if (logger.isDebugEnabled()) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - before) / 100000) / 10;
            this.before = timeStamp;
            logger.debug("[{}] transmit => SERESPONSE = {}, elapsed {} ms.", this.getName(),
                    seResponse.toString(), elapsedMs);
        }

        return seResponse;
    }

    /**
     * Abstract method implemented by the AbstractLocalReader and VirtualReader classes.
     * <p>
     * This method is handled by transmit.
     * 
     * @param seRequest the SeRequestSet to be processed
     * @return the SeResponse (responses to the SeRequest)
     * @throws KeypleReaderException if reader error occurs
     */
    protected abstract SeResponse processSeRequest(SeRequest seRequest)
            throws KeypleReaderException;

    /** ==== Methods specific to observability ============================= */

    /**
     * Starts the monitoring of the reader activity (especially card insertion and removal)
     * <p>
     * This abstract method has to be implemented by the class that handle the monitoring thread (
     * e.g. {@link AbstractThreadedLocalReader}).
     * <p>
     * It will be called when a first observer is added (see addObserver).
     */
    protected abstract void startObservation();

    /**
     * Ends the monitoring of the reader activity
     * <p>
     * This abstract method has to be implemented by the class that handle the monitoring thread
     * (e.g. {@link AbstractThreadedLocalReader}). It will be called when the observer is removed.
     * <p>
     * It will be called when the last observer is removed (see removeObserver).
     *
     */
    protected abstract void stopObservation();

    /**
     * Add a reader observer.
     * <p>
     * The observer will receive all the events produced by this reader (card insertion, removal,
     * etc.)
     * <p>
     * The startObservation() is called when the first observer is added. (to start a monitoring
     * thread for instance)
     *
     * @param observer the observer object
     */
    public final void addObserver(ReaderObserver observer) {
        // if an observer is added to an empty list, start the observation
        if (super.countObservers() == 0) {
            logger.debug("Start the reader monitoring.");
            startObservation();
        }
        super.addObserver(observer);
    }

    /**
     * Remove a reader observer.
     * <p>
     * The observer will not receive any of the events produced by this reader.
     * <p>
     * The stopObservation() is called when the last observer is removed. (to stop a monitoring
     * thread for instance)
     *
     * @param observer the observer object
     */
    public final void removeObserver(ReaderObserver observer) {
        super.removeObserver(observer);
        if (super.countObservers() == 0) {
            logger.debug("Stop the reader monitoring.");
            stopObservation();
        }
    }
}
