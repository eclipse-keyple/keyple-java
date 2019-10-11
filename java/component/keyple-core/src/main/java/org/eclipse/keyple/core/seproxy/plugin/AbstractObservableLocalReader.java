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

import org.eclipse.keyple.core.seproxy.ChannelState;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
 * transmission made with a CLOSE_AND_CONTINUE or CLOSE_AND_AND_STOP parameter, or by an explicit
 * call to the notifySeProcessed method (if the latter is called when a "CLOSE" transmission has
 * already been made, it will do nothing, otherwise it will make a pseudo transmission intended only
 * for closing channels).
 * <p>
 * If the instruction given is CLOSE_AND_STOP then the logical and physical channels are closed
 * immediately and the Machine to state changes to WAIT_FOR_START_DETECTION state.
 * <p>
 * If the instruction given is CLOSE_AND_CONTINUE then the state machine changes to
 * WAIT_FOR_SE_REMOVAL.
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

    /** The states that the reader monitoring state machine can have */
    protected enum MonitoringState {
        WAIT_FOR_START_DETECTION, WAIT_FOR_SE_INSERTION, WAIT_FOR_SE_PROCESSING, WAIT_FOR_SE_REMOVAL
    }

    protected abstract void startRemovalSequence(ChannelState channelState);
}
