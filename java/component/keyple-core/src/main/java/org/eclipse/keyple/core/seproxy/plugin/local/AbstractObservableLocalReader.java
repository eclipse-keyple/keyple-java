/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.core.seproxy.plugin.local;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleChannelControlException;
import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsResponse;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This abstract class is used to manage the matter of observing SE events in the case of a local
 * reader.
 *
 * <p>
 * It provides the means to configure the plugin's behavior when a SE is detected.
 *
 * The event management implements a ObservableReaderStateService state machine that is composed of
 * four states.
 * <ol>
 * <li>WAIT_FOR_START_DETECTION
 * <p>
 * Infinitely waiting for a signal from the application to start SE detection by changing to
 * WAIT_FOR_SE_INSERTION state. This signal is given by calling the setDefaultSelectionRequest
 * method.
 * <p>
 * Note: The system always starts in the WAIT_FOR_START_DETECTION state.
 * <li>WAIT_FOR_SE_INSERTION
 * <p>
 * Awaiting the SE insertion. After insertion, the processSeInserted method is called.
 * <p>
 * A number of cases arise:
 * <ul>
 * <li>A default selection is defined: in this case it is played and its result leads to an event
 * notification SE_INSERTED or SE_MATCHED or no event (see setDefaultSelectionRequest)
 *
 * <li>There is no default selection: a SE_INSERTED event is then notified.
 * <p>
 * In the case where an event has been notified to the application, the state machine changes to the
 * WAIT_FOR_SE_PROCESSING state otherwise it remains in the WAIT_FOR_SE_INSERTION state.
 * </ul>
 * <p>
 * The notification consists in calling the "update" methods of the defined observers. In the case
 * where several observers have been defined, it is up to the application developer to ensure that
 * there is no long processing in these methods, by making their execution asynchronous for example.
 * <li>WAIT_FOR_SE_PROCESSING
 * <p>
 * Waiting for the end of processing by the application. The end signal is triggered either by a
 * transmission made with a CLOSE_AFTER parameter, or by an explicit call to the notifySeProcessed
 * method (if the latter is called when a "CLOSE" transmission has already been made, it will do
 * nothing, otherwise it will make a pseudo transmission intended only for closing channels).
 * <p>
 * If the instruction given when defining the default selection request is to stop
 * (ObservableReader.PollingMode.SINGLESHOT) then the logical and physical channels are closed
 * immediately and the machine state changes to WAIT_FOR_START_DETECTION state.
 * <p>
 * If the instruction given is continue (ObservableReader.PollingMode.REPEATING) then the state
 * machine changes to WAIT_FOR_SE_REMOVAL.
 * <li>WAIT_FOR_SE_REMOVAL:
 * <p>
 * Waiting for the SE to be removed. When the SE is removed, a SE_REMOVED event is notified to the
 * application and the state machine changes to the WAIT_FOR_SE_INSERTION or
 * WAIT_FOR_START_DETECTION state according the polling mode (ObservableReader.PollingMode).
 * </ol>
 */
public abstract class AbstractObservableLocalReader extends AbstractLocalReader
        implements ObservableReader {
    /** logger */
    private static final Logger logger =
            LoggerFactory.getLogger(AbstractObservableLocalReader.class);


    /** The default DefaultSelectionsRequest to be executed upon SE insertion */
    private DefaultSelectionsRequest defaultSelectionsRequest;

    /** Indicate if all SE detected should be notified or only matching SE */
    private ObservableReader.NotificationMode notificationMode;

    private ObservableReader.PollingMode currentPollingMode =
            ObservableReader.PollingMode.SINGLESHOT;

    /* Internal events */
    public enum InternalEvent {
        /** A SE has been inserted */
        SE_INSERTED,
        /** The SE has been removed */
        SE_REMOVED,
        /** The application has completed the processing of the SE */
        SE_PROCESSED,
        /** The application has requested the start of SE detection */
        START_DETECT,
        /** The application has requested that SE detection is to be stopped. */
        STOP_DETECT,
        /** A timeout has occurred (not yet implemented) */
        TIME_OUT
    }

    /* The observers of this object */
    private Set<ObservableReader.ReaderObserver> observers;
    /*
     * this object will be used to synchronize the access to the observers list in order to be
     * thread safe
     */
    private final Object sync = new Object();

    /* Service that handles Internal Events and their impact on the current state of the reader */
    protected ObservableReaderStateService stateService;


    /**
     * Initialize the ObservableReaderStateService with the possible states and their
     * implementation. ObservableReaderStateService define the initial state.
     * <p>
     * Make sure to initialize the stateService in your reader constructor with stateService =
     * initStateService()
     * <p>
     *
     * @return initialized state stateService with possible states and the init state
     */
    protected abstract ObservableReaderStateService initStateService();

    /**
     * Reader constructor
     * <p>
     * Force the definition of a name through the use of super method.
     * <p>
     *
     * @param pluginName the name of the plugin that instantiated the reader
     * @param readerName the name of the reader
     */
    public AbstractObservableLocalReader(String pluginName, String readerName) {
        super(pluginName, readerName);
    }

    /**
     * Add a {@link ObservableReader.ReaderObserver}.
     * <p>
     * The observer will receive all the events produced by this reader (se insertion, removal,
     * etc.)
     *
     * @param observer the observer object
     */
    @Override
    public final void addObserver(final ObservableReader.ReaderObserver observer) {
        if (observer == null) {
            return;
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Adding '{}' as an observer of '{}'.", observer.getClass().getSimpleName(),
                    getName());
        }

        synchronized (sync) {
            if (observers == null) {
                observers = new HashSet<ObservableReader.ReaderObserver>(1);
            }
            observers.add(observer);
        }
    }

    /**
     * Remove a {@link ObservableReader.ReaderObserver}.
     * <p>
     * The observer will do not receive any of the events produced by this reader.
     *
     * @param observer the observer object
     */
    @Override
    public final void removeObserver(final ObservableReader.ReaderObserver observer) {
        if (observer == null) {
            return;
        }

        if (logger.isTraceEnabled()) {
            logger.trace("[{}] Deleting a reader observer", getName());
        }

        synchronized (sync) {
            if (observers != null) {
                observers.remove(observer);
            }
        }
    }

    /**
     * Notify all registered observers with the provided {@link ReaderEvent}
     *
     * @param event the reader event
     */
    @Override
    public final void notifyObservers(final ReaderEvent event) {

        if (logger.isTraceEnabled()) {
            logger.trace("[{}] Notifying a reader event to {} observers. EVENTNAME = {}", getName(),
                    this.countObservers(), event.getEventType().getName());
        }

        Set<ObservableReader.ReaderObserver> observersCopy;

        synchronized (sync) {
            if (observers == null) {
                return;
            }
            observersCopy = new HashSet<ObservableReader.ReaderObserver>(observers);
        }

        for (ObservableReader.ReaderObserver observer : observersCopy) {
            observer.update(event);
        }
    }

    /**
     * @return the number of observers
     */
    @Override
    public final int countObservers() {
        return observers == null ? 0 : observers.size();
    }

    /**
     * Remove all observers at once
     */
    @Override
    public final void clearObservers() {
        if (observers != null) {
            this.observers.clear();
        }
    }

    /**
     * Allows the application to signal the end of processing and thus proceed with the removal
     * sequence, followed by a restart of the card search.
     * <p>
     * Do nothing if the closing of the physical channel has already been requested.
     * <p>
     * Send a request without APDU just to close the physical channel if it has not already been
     * closed.
     *
     */
    public final void notifySeProcessed() {
        if (forceClosing) {
            try {
                // close the physical channel thanks to CLOSE_AFTER flag
                processSeRequest(null, ChannelControl.CLOSE_AFTER);
                if (logger.isTraceEnabled()) {
                    logger.trace(
                            "Explicit communication closing requested, starting removal sequence.");
                }
            } catch (KeypleReaderException e) {
                logger.error("KeypleReaderException while terminating. {}", e.getMessage());
            }
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Explicit physical channel closing already requested.");
            }
        }
    }

    /**
     * Check the presence of a SE
     * <p>
     * This method is recommended for non-observable readers.
     * <p>
     * When the SE is not present the logical and physical channels status may be refreshed through
     * a call to the processSeRemoved method.
     *
     * @return true if the SE is present
     */
    @Deprecated // will change in a later version
    @Override
    public final boolean isSePresent() throws KeypleIOReaderException {
        if (checkSePresence()) {
            return true;
        } else {
            /*
             * if the SE is no longer present but one of the channels is still open, then the
             * SE_REMOVED notification is performed and the channels are closed.
             */
            if (isLogicalChannelOpen() || isPhysicalChannelOpen()) {
                processSeRemoved();
            }
            return false;
        }
    }


    /**
     * Starts the SE detection. Once activated, the application can be notified of the arrival of an
     * SE.
     * <p>
     * This method must be overloaded by readers depending on the particularity of their management
     * of the start of SE detection.
     * <p>
     * Note: they must call the super method with the argument PollingMode.
     *
     * @param pollingMode indicates the action to be followed after processing the SE: if REPEATING,
     *        the SE detection is restarted, if SINGLESHOT, the SE detection is stopped until a new
     *        call to startSeDetection is made.
     */
    public final void startSeDetection(ObservableReader.PollingMode pollingMode) {
        if (logger.isTraceEnabled()) {
            logger.trace("[{}] start Se Detection with pollingMode {}", getName(), pollingMode);
        }
        this.currentPollingMode = pollingMode;
        this.stateService.onEvent(InternalEvent.START_DETECT);
    }

    /**
     * Stops the SE detection.
     * <p>
     * This method must be overloaded by readers depending on the particularity of their management
     * of the start of SE detection.
     */
    public final void stopSeDetection() {
        if (logger.isTraceEnabled()) {
            logger.trace("[{}] stop Se Detection", getName());
        }
        this.stateService.onEvent(InternalEvent.STOP_DETECT);
    }

    /**
     * If defined, the prepared DefaultSelectionRequest will be processed as soon as a SE is
     * inserted. The result of this request set will be added to the reader event notified to the
     * application.
     * <p>
     * If it is not defined (set to null), a simple SE detection will be notified in the end.
     * <p>
     * Depending on the notification mode, the observer will be notified whenever an SE is inserted,
     * regardless of the selection status, or only if the current SE matches the selection criteria.
     * <p>
     *
     * @param defaultSelectionsRequest the {@link AbstractDefaultSelectionsRequest} to be executed
     *        when a SE is inserted
     * @param notificationMode the notification mode enum (ALWAYS or MATCHED_ONLY)
     */
    public void setDefaultSelectionRequest(
            AbstractDefaultSelectionsRequest defaultSelectionsRequest,
            ObservableReader.NotificationMode notificationMode) {
        this.defaultSelectionsRequest = (DefaultSelectionsRequest) defaultSelectionsRequest;
        this.notificationMode = notificationMode;
    }

    /**
     * A combination of defining the default selection request and starting the SE detection.
     *
     * @param defaultSelectionsRequest the selection request to be operated
     * @param notificationMode indicates whether a SE_INSERTED event should be notified even if the
     *        selection has failed (ALWAYS) or whether the SE insertion should be ignored in this
     *        case (MATCHED_ONLY).
     * @param pollingMode indicates the action to be followed after processing the SE: if CONTINUE,
     *        the SE detection is restarted, if STOP, the SE detection is stopped until a new call
     *        to startSeDetection is made.
     */
    public void setDefaultSelectionRequest(
            AbstractDefaultSelectionsRequest defaultSelectionsRequest,
            ObservableReader.NotificationMode notificationMode,
            ObservableReader.PollingMode pollingMode) {
        // define the default selection request
        setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode);
        // initiates the SE detection
        startSeDetection(pollingMode);
    }



    /**
     * This method initiates the SE removal sequence.
     * <p>
     * The reader will remain in the WAIT_FOR_SE_REMOVAL state as long as the SE is present. It will
     * change to the WAIT_FOR_START_DETECTION or WAIT_FOR_SE_INSERTION state depending on what was
     * set when the detection was started.
     */
    @Deprecated // will change in a later version
    protected void startRemovalSequence() {
        if (logger.isTraceEnabled()) {
            logger.trace("[{}] start removal sequence of the reader", getName());
        }
        this.stateService.onEvent(InternalEvent.SE_PROCESSED);
    }


    /**
     * This method is invoked when a SE is inserted in the case of an observable reader.
     * <p>
     * e.g. from the monitoring thread in the case of a Pcsc plugin or from the NfcAdapter callback
     * method onTagDiscovered in the case of a Android NFC plugin.
     * <p>
     * It will return a ReaderEvent in the following cases:
     * <ul>
     * <li>SE_INSERTED: if no default selection request was defined
     * <li>SE_MATCHED: if a default selection request was defined in any mode and a SE matched the
     * selection
     * <li>SE_INSERTED: if a default selection request was defined in ALWAYS mode but no SE matched
     * the selection (the DefaultSelectionsResponse is however transmitted)
     * </ul>
     * <p>
     * It returns null if a default selection is defined in MATCHED_ONLY mode but no SE matched the
     * selection.
     *
     * @return ReaderEvent that should be notified to observers, contains the results of the default
     *         selection if any, can be null if no event should be sent
     */
    @Deprecated // will change in a later version
    public final ReaderEvent processSeInserted() {
        if (logger.isTraceEnabled()) {
            logger.trace("[{}] process the inserted se", getName());
        }
        if (defaultSelectionsRequest == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("[{}] no default selection request defined, notify SE_INSERTED",
                        getName());
            }
            /* no default request is defined, just notify the SE insertion */
            return new ReaderEvent(getPluginName(), getName(), ReaderEvent.EventType.SE_INSERTED,
                    null);
        } else {
            /*
             * a default request is defined, send it and notify according to the notification mode
             * and the selection status
             */
            boolean aSeMatched = false;
            try {
                List<SeResponse> seResponseList =
                        transmitSet(defaultSelectionsRequest.getSelectionSeRequestSet(),
                                defaultSelectionsRequest.getMultiSeRequestProcessing(),
                                defaultSelectionsRequest.getChannelControl());

                for (SeResponse seResponse : seResponseList) {
                    if (seResponse != null && seResponse.getSelectionStatus().hasMatched()) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("[{}] a default selection has matched", getName());
                        }
                        aSeMatched = true;
                        break;
                    }
                }

                if (notificationMode == ObservableReader.NotificationMode.MATCHED_ONLY) {
                    /* notify only if a SE matched the selection, just ignore if not */
                    if (aSeMatched) {
                        return new ReaderEvent(getPluginName(), getName(),
                                ReaderEvent.EventType.SE_MATCHED,
                                new DefaultSelectionsResponse(seResponseList));
                    } else {
                        if (logger.isTraceEnabled()) {
                            logger.trace("[{}] selection hasn't matched"
                                    + " do not thrown any event because of MATCHED_ONLY flag",
                                    getName());
                        }
                        return null;
                    }
                } else {
                    // ObservableReader.NotificationMode.ALWAYS
                    if (aSeMatched) {
                        /* The SE matched, notify a SE_MATCHED event with the received response */
                        return new ReaderEvent(getPluginName(), getName(),
                                ReaderEvent.EventType.SE_MATCHED,
                                new DefaultSelectionsResponse(seResponseList));
                    } else {
                        /*
                         * The SE didn't match, notify an SE_INSERTED event with the received
                         * response
                         */
                        if (logger.isTraceEnabled()) {
                            logger.trace("[{}] none of {} default selection matched", getName(),
                                    seResponseList.size());
                        }
                        return new ReaderEvent(getPluginName(), getName(),
                                ReaderEvent.EventType.SE_INSERTED,
                                new DefaultSelectionsResponse(seResponseList));
                    }
                }
            } catch (KeypleReaderException e) {
                /* the last transmission failed, close the logical and physical channels */
                closeLogicalAndPhysicalChannels();
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "An IO Exception occurred while processing the default selection. {}",
                            e.getMessage());
                }
                // in this case the SE has been removed or not read correctly, do not throw event
            }
        }

        // We close here the physical channel in case it has been opened for a SE outside the
        // expected SEs
        try {
            closePhysicalChannel();
        } catch (KeypleChannelControlException e) {
            logger.error("Error while closing physical channel. {}", e.getMessage());
        }
        // no event returned
        return null;
    }

    /**
     * Sends a neutral APDU to the SE to check its presence. The status of the response is not
     * verified as long as the mere fact that the SE responds is sufficient to indicate whether or
     * not it is present.
     * <p>
     * This method has to be called regularly until the SE no longer respond.
     * <p>
     * Having this method not final allows a reader plugin to implement its own method.
     *
     * @return true if the SE still responds, false if not
     */
    @Deprecated // will change in a later version
    public boolean isSePresentPing() {
        // APDU sent to check the communication with the PO
        final byte[] apdu = {(byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        // transmits the APDU and checks for the IO exception.
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("[{}] Ping SE", getName());
            }
            transmitApdu(apdu);
        } catch (KeypleIOReaderException e) {
            if (logger.isTraceEnabled()) {
                logger.trace("[{}] Exception occurred in isSePresentPing. Message: {}", getName(),
                        e.getMessage());
            }
            return false;
        }
        return true;
    }


    /**
     * This method is invoked when a SE is removed in the case of an observable reader.
     * <p>
     * It will also be invoked if isSePresent is called and at least one of the physical or logical
     * channels is still open (case of a non-observable reader)
     * <p>
     * The SE will be notified removed only if it has been previously notified present (observable
     * reader only)
     *
     */
    @Deprecated // will change in a later version
    public final void processSeRemoved() {
        closeLogicalAndPhysicalChannels();
        notifyObservers(new ReaderEvent(getPluginName(), getName(),
                ReaderEvent.EventType.SE_REMOVED, null));
    }

    /**
     * Get polling mode
     * 
     * @return the current polling mode
     */
    @Deprecated // will change in a later version
    public ObservableReader.PollingMode getPollingMode() {
        return currentPollingMode;
    }


    /**
     * Changes the state of the state machine
     *
     * @param stateId : new stateId
     */
    @Deprecated // will change in a later version
    protected void switchState(AbstractObservableState.MonitoringState stateId) {
        this.stateService.switchState(stateId);
    }

    /**
     * Get the current monitoring state
     * 
     * @return current getMonitoringState
     */
    @Deprecated // will change in a later version
    public AbstractObservableState.MonitoringState getCurrentMonitoringState() {
        return this.stateService.getCurrentMonitoringState();
    }

    /**
     * thread safe method to communicate an internal event to this reader Use this method to inform
     * the reader of external event like a tag discovered or a Se inserted
     *
     * @param event internal event
     */
    @Deprecated // will change in a later version
    public void onEvent(InternalEvent event) {
        this.stateService.onEvent(event);
    }


}
