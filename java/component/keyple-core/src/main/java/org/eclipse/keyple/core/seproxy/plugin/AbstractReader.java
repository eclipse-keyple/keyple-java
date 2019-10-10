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
package org.eclipse.keyple.core.seproxy.plugin;


import java.util.List;
import java.util.Set;
import org.eclipse.keyple.core.seproxy.ChannelState;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.event.ObservableReader.NotificationMode;
import org.eclipse.keyple.core.seproxy.event.ObservableReader.ReaderObserver;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleChannelStateException;
import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract definition of an observable reader.
 * <ul>
 * <li>High level logging and benchmarking of Set of SeRequest and SeRequest transmission</li>
 * <li>Observability management</li>
 * <li>Name-based comparison of ProxyReader (required for SortedSet&lt;ProxyReader&gt;)</li>
 * <li>Plugin naming management</li>
 * </ul>
 */

public abstract class AbstractReader extends AbstractLoggedObservable<ReaderEvent>
        implements ProxyReader {

    /** logger */
    private static final Logger logger = LoggerFactory.getLogger(AbstractReader.class);

    /** Timestamp recorder */
    private long before;

    /** Contains the name of the plugin */
    protected final String pluginName;

    /** The default DefaultSelectionsRequest to be executed upon SE insertion */
    protected DefaultSelectionsRequest defaultSelectionsRequest;

    /** Indicate if all SE detected should be notified or only matching SE */
    protected NotificationMode notificationMode;

    /**
     * This flag is used with transmit or transmitSet
     * <p>
     * It will be used by the terminate method to determine if a command to close the physical
     * channel has been already requested and therefore to switch directly to the removal sequence
     * for the observed readers.
     */
    private boolean forceClosing = true;

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
    protected AbstractReader(String pluginName, String readerName) {
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
     * The global execution time (inter-exchange and communication) and the Set of SeRequest content
     * is logged (DEBUG level).
     * <p>
     * As the method is final, it cannot be extended.
     *
     * @param requestSet the request set
     * @return responseSet the response set
     * @throws KeypleReaderException if a reader error occurs
     */
    public final List<SeResponse> transmitSet(Set<SeRequest> requestSet,
            MultiSeRequestProcessing multiSeRequestProcessing, ChannelState channelState)
            throws KeypleReaderException {
        if (requestSet == null) {
            throw new IllegalArgumentException("seRequestSet must not be null");
        }

        /* sets the forceClosing flag */
        forceClosing = channelState == ChannelState.KEEP_OPEN;

        List<SeResponse> responseSet;

        if (logger.isDebugEnabled()) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.debug("[{}] transmit => SEREQUESTSET = {}, elapsed {} ms.", this.getName(),
                    requestSet.toString(), elapsedMs);
        }

        try {
            responseSet = processSeRequestSet(requestSet, multiSeRequestProcessing, channelState);
        } catch (KeypleChannelStateException ex) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.debug("[{}] transmit => SEREQUESTSET channel failure. elapsed {}",
                    this.getName(), elapsedMs);
            /* Throw an exception with the responses collected so far. */
            throw ex;
        } catch (KeypleIOReaderException ex) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.debug("[{}] transmit => SEREQUESTSET IO failure. elapsed {}", this.getName(),
                    elapsedMs);
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

    public final List<SeResponse> transmitSet(Set<SeRequest> requestSet)
            throws KeypleReaderException {
        return transmitSet(requestSet, MultiSeRequestProcessing.FIRST_MATCH,
                ChannelState.KEEP_OPEN);
    }

    /**
     * Abstract method implemented by the AbstractLocalReader and VirtualReader classes.
     * <p>
     * This method is handled by transmitSet.
     *
     * @param requestSet the Set of {@link SeRequest} to be processed
     * @param multiSeRequestProcessing the multi se processing mode
     * @param channelState indicates if the channel has to be closed at the end of the processing
     * @return the List of {@link SeResponse} (responses to the Set of {@link SeRequest})
     * @throws KeypleReaderException if reader error occurs
     */
    protected abstract List<SeResponse> processSeRequestSet(Set<SeRequest> requestSet,
            MultiSeRequestProcessing multiSeRequestProcessing, ChannelState channelState)
            throws KeypleReaderException;

    /**
     * Execute the transmission of a {@link SeRequest} and returns a {@link SeResponse}
     * <p>
     * The individual execution time (inter-exchange and communication) and the {@link SeRequest}
     * content is logged (DEBUG level).
     * <p>
     * As the method is final, it cannot be extended.
     *
     * @param seRequest the request to be transmitted
     * @param channelState indicates if the channel has to be closed at the end of the processing
     * @return the received response
     * @throws KeypleReaderException if a reader error occurs
     */
    public final SeResponse transmit(SeRequest seRequest, ChannelState channelState)
            throws KeypleReaderException {
        if (seRequest == null) {
            throw new IllegalArgumentException("seRequest must not be null");
        }

        /* sets the forceClosing flag */
        forceClosing = channelState == ChannelState.KEEP_OPEN;

        SeResponse seResponse;

        if (logger.isDebugEnabled()) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.debug("[{}] transmit => SEREQUEST = {}, elapsed {} ms.", this.getName(),
                    seRequest.toString(), elapsedMs);
        }

        try {
            seResponse = processSeRequest(seRequest, channelState);
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

    public final SeResponse transmit(SeRequest seRequest) throws KeypleReaderException {
        return transmit(seRequest, ChannelState.KEEP_OPEN);
    }

    /**
     * Abstract method implemented by the AbstractLocalReader and VirtualReader classes.
     * <p>
     * This method is handled by transmit.
     *
     * @param seRequest the {@link SeRequest} to be processed
     * @param channelState a flag indicating if the channel has to be closed after the processing of
     *        the {@link SeRequest}
     * @return the {@link SeResponse} (responses to the {@link SeRequest})
     * @throws KeypleReaderException if reader error occurs
     */
    protected abstract SeResponse processSeRequest(SeRequest seRequest, ChannelState channelState)
            throws KeypleReaderException;

    /** ==== Methods specific to observability ============================= */

    /**
     * Allows the application to signal the end of processing and thus proceed with the removal
     * sequence, followed by a restart of the card search.
     * <p>
     * Do nothing if the closing of the physical channel has already been requested.
     * <p>
     * Send a request without APDU just to close the physical channel if it has not already been
     * closed.
     * 
     * @param channelState indicates the action to be taken after the closing of the physical
     *        channel (continue to wait for SE or stop)
     */
    public void notifySeProcessed(ChannelState channelState) {
        if (forceClosing) {
            if (channelState == ChannelState.KEEP_OPEN) {
                throw new IllegalArgumentException(
                        "The physical channel cannot be kept open after the SE processing.");
            }
            try {
                processSeRequest(null, channelState);
                logger.trace("Explicit physical channel closing executed.");
            } catch (KeypleReaderException e) {
                logger.error("KeypleReaderException while terminating." + e.getMessage());
            }
        } else {
            logger.trace("Explicit physical channel closing already requested.");
        }
    }

    /**
     * Add a reader observer.
     * <p>
     * The observer will receive all the events produced by this reader (card insertion, removal,
     * etc.)
     *
     * @param observer the observer object
     */
    public void addObserver(ReaderObserver observer) {
        super.addObserver(observer);
    }

    /**
     * Remove a reader observer.
     * <p>
     * The observer will not receive any of the events produced by this reader.
     *
     * @param observer the observer object
     */
    public void removeObserver(ReaderObserver observer) {
        super.removeObserver(observer);
    }
}