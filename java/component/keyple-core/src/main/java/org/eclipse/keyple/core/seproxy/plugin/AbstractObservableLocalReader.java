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
package org.eclipse.keyple.core.seproxy.plugin;

import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleChannelControlException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsResponse;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * This abstract class is used to manage the matter of observing SE events in the case of a local
 * reader.
 *
 * <p>
 * It provides the means to configure the plugin's behavior when a SE is detected.
 * <p>
 * Thus, it is possible to preload the so-called default selection, played directly by the plugin as
 * soon as the OS has been introduced.
 * <p>
 * Two outputs of this processing are available depending on whether one wants to be notified of all
 * SE arrivals (ObservableReader.NotificationMode.ALWAYS) or only those that lead to a logical
 * channel opening (ObservableReader.NotificationMode.MATCHED_ONLY).
 * <p>
 * The monitoring of these events is either directly implemented by the plugin (e. g. Android NFC
 * plugin) or implemented using the child class {@link AbstractThreadedObservableLocalReader}
 * <p>
 * The event management implements a state machine that is composed of four states.
 * <ol>
 * <li>WAIT_FOR_START_DETECTION
 * <p>
 * Infinitely waiting for a signal from the application to start SE detection by changing to
 * WAIT_FOR_SE_INSERTION state. This signal is given by calling the setDefaultSelectionRequest
 * method. Note: The system always starts directly in the WAIT_FOR_SE_INSERTION state.</li>
 * <li>WAIT_FOR_SE_INSERTION
 * <p>
 * Awaiting the SE insertion. After insertion, the processSeInserted method is called.
 * <p>
 * A number of cases arise:
 * <ul>
 * <li>A default selection is defined: in this case it is played and its result leads to an event
 * notification SE_INSERTED or SE_MATCHED or no event (see setDefaultSelectionRequest)</li>
 *
 * <li>There is no default selection: a SE_INSERTED event is then notified.
 * <p>
 * In the case where an event has been notified to the application, the state machine changes to the
 * WAIT_FOR_SE_PROCESSING state otherwise it changes to the WAIT_FOR_SE_REMOVAL state.</li>
 * </ul>
 * <p>
 * The notification consists in calling the "update" methods of the defined observers. In the case
 * where several observers have been defined, it is up to the application developer to ensure that
 * there is no long processing in these methods, by making their execution asynchronous for
 * example.</li>
 * <li>WAIT_FOR_SE_PROCESSING:
 * <p>
 * Waiting for the end of processing by the application. The end signal is triggered either by a
 * transmission made with a CLOSE_AFTER parameter, or by an explicit call to the notifySeProcessed
 * method (if the latter is called when a "CLOSE" transmission has already been made, it will do
 * nothing, otherwise it will make a pseudo transmission intended only for closing channels).
 * <p>
 * If the instruction given when defining the default selection request is STOP then the logical and
 * physical channels are closed immediately and the Machine to state changes to
 * WAIT_FOR_START_DETECTION state.
 * <p>
 * If the instruction given is CONTINUE then the state machine changes to WAIT_FOR_SE_REMOVAL.
 * <p>
 * A timeout management is also optionally present in order to avoid a lock in this waiting state
 * due to a failure of the application that would have prevented it from notifying the end of SE
 * processing (see setThreadWaitTimeout).</li>
 * <li>WAIT_FOR_SE_REMOVAL:
 * <p>
 * Waiting for the SE to be removed. When the SE is removed, a SE_REMOVED event is notified to the
 * application and the state machine changes to the WAIT_FOR_SE_INSERTION state.
 * <p>
 * A timeout management is also optionally present in order to avoid a lock in this waiting state
 * due to a SE forgotten on the reader. *</li>
 * </ol>
 */
public abstract class AbstractObservableLocalReader extends AbstractLocalReader {
    /** logger */
    private static final Logger logger =
            LoggerFactory.getLogger(AbstractObservableLocalReader.class);


    /** The default DefaultSelectionsRequest to be executed upon SE insertion */
    protected DefaultSelectionsRequest defaultSelectionsRequest;

    /** Indicate if all SE detected should be notified or only matching SE */
    protected ObservableReader.NotificationMode notificationMode;

    protected ObservableReader.PollingMode currentPollingMode = ObservableReader.PollingMode.STOP;

    protected AbstractObservableState state;

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
        state = getInitState();
    }


    /**
     * Check the presence of a SE
     * <p>
     * This method is recommended for non-observable readers.
     * <p>
     * When the card is not present the logical and physical channels status may be refreshed
     * through a call to the processSeRemoved method.
     *
     * @return true if the SE is present
     */
    @Override
    public boolean isSePresent() throws NoStackTraceThrowable {
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
     * @param pollingMode indicates the action to be followed after processing the SE: if CONTINUE,
     *        the SE detection is restarted, if STOP, the SE detection is stopped until a new call
     *        to startSeDetection is made.
     */
    public void startSeDetection(ObservableReader.PollingMode pollingMode) {
        currentPollingMode = pollingMode;
        state = state.onStartDetection();
    }//TODO OD : shouldn't this method be in ThreadedObs?

    /**
     * Stops the SE detection.
     * <p>
     * This method must be overloaded by readers depending on the particularity of their management
     * of the start of SE detection.
     * <p>
     */
    public void stopSeDetection() {
        state = state.onStopDetection();
    }//TODO OD : shouldn't this method be in ThreadedObs?

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
     * In addition, in the case of a {@link AbstractThreadedObservableLocalReader} the observation
     * thread will be notified of request to start the SE insertion monitoring (change from the
     * WAIT_FOR_START_DETECTION state to WAIT_FOR_SE_INSERTION).
     * <p>
     * An {@link java.lang.IllegalStateException} exception will be thrown if no observers have been
     * recorded for this reader (see startMonitoring).
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

    /** The states that the reader monitoring state machine can have */
    protected enum MonitoringState {
        WAIT_FOR_START_DETECTION, WAIT_FOR_SE_INSERTION, WAIT_FOR_SE_PROCESSING, WAIT_FOR_SE_REMOVAL
    }

    /**
     * This method initiates the SE removal sequence.
     * <p>
     * The reader will remain in the WAIT_FOR_SE_REMOVAL state as long as the SE is present. It will
     * change to the WAIT_FOR_START_DETECTION or WAIT_FOR_SE_INSERTION state depending on what was
     * set when the detection was started.
     */
    protected void startRemovalSequence(){
        state = state.onSeProcessed();
    };


    /**
     * This method is invoked when a SE is inserted in the case of an observable reader.
     * <p>
     * e.g. from the monitoring thread in the case of a Pcsc plugin or from the NfcAdapter callback
     * method onTagDiscovered in the case of a Android NFC plugin.
     * <p>
     * It will fire an ReaderEvent in the following cases:
     * <ul>
     * <li>SE_INSERTED: if no default selection request was defined</li>
     * <li>SE_MATCHED: if a default selection request was defined in any mode and a SE matched the
     * selection</li>
     * <li>SE_INSERTED: if a default selection request was defined in ALWAYS mode but no SE matched
     * the selection (the DefaultSelectionsResponse is however transmitted)</li>
     * </ul>
     * <p>
     * It will do nothing if a default selection is defined in MATCHED_ONLY mode but no SE matched
     * the selection.
     *
     * @return true if the notification was actually sent to the application, false if not
     */
    protected final boolean processSeInserted() {
        boolean presenceNotified = false;
        if (defaultSelectionsRequest == null) {
            /* no default request is defined, just notify the SE insertion */
            notifyObservers(new ReaderEvent(this.pluginName, this.name,
                    ReaderEvent.EventType.SE_INSERTED, null));
            presenceNotified = true;
            state = state.onSeInserted();
        } else {
            /*
             * a default request is defined, send it a notify according to the notification mode and
             * the selection status
             */
            boolean aSeMatched = false;
            try {
                List<SeResponse> seResponseList =
                        transmitSet(defaultSelectionsRequest.getSelectionSeRequestSet(),
                                defaultSelectionsRequest.getMultiSeRequestProcessing(),
                                defaultSelectionsRequest.getChannelControl());

                for (SeResponse seResponse : seResponseList) {
                    if (seResponse != null && seResponse.getSelectionStatus().hasMatched()) {
                        logger.trace("[{}] processSeInserted => a default selection has matched", this.getName());
                        aSeMatched = true;
                        break;
                    }
                    logger.trace("[{}] processSeInserted => none of {} default selection matched", this.getName(), seResponseList.size());
                }
                if (notificationMode == ObservableReader.NotificationMode.MATCHED_ONLY) {
                    /* notify only if a SE matched the selection, just ignore if not */
                    if (aSeMatched) {
                        notifyObservers(new ReaderEvent(this.pluginName, this.name,
                                ReaderEvent.EventType.SE_MATCHED,
                                new DefaultSelectionsResponse(seResponseList)));
                        presenceNotified = true;
                        state = state.onSeInserted();
                    }else{
                        logger.trace("[{}] processSeInserted => selection hasn't matched do not thrown any event because of MATCHED_ONLY flag");
                    }
                } else {
                    //ObservableReader.NotificationMode.ALWAYS
                    if (aSeMatched) {
                        /* The SE matched, notify an SE_MATCHED event with the received response */
                        notifyObservers(new ReaderEvent(this.pluginName, this.name,
                                ReaderEvent.EventType.SE_MATCHED,
                                new DefaultSelectionsResponse(seResponseList)));
                    } else {
                        /*
                         * The SE didn't match, notify an SE_INSERTED event with the received
                         * response
                         */
                        notifyObservers(new ReaderEvent(this.pluginName, this.name,
                                ReaderEvent.EventType.SE_INSERTED,
                                new DefaultSelectionsResponse(seResponseList)));
                    }
                    state = state.onSeInserted();
                    presenceNotified = true;
                }
            } catch (KeypleReaderException e) {
                /* the last transmission failed, close the logical and physical channels */
                closeLogicalAndPhysicalChannels();
                logger.debug("An IO Exception occurred while processing the default selection. {}",
                        e.getMessage());
                // in this case the card has been removed or not read correctly, do not throw event
            }
        }

        if (!presenceNotified) {
            // We close here the physical channel in case it has been opened for a SE outside the
            // expected SEs
            try {
                closePhysicalChannel();
            } catch (KeypleChannelControlException e) {
                logger.error("Error while closing physical channel. {}", e.getMessage());
            }
        }

        return presenceNotified;
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
    protected final void processSeRemoved() {
        closeLogicalAndPhysicalChannels();
        notifyObservers(new ReaderEvent(this.pluginName, this.name,
                ReaderEvent.EventType.SE_REMOVED, null));
        state = state.onSeRemoved();
    }

    abstract protected AbstractObservableState getInitState();
}
