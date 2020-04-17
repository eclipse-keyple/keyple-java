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
package org.eclipse.keyple.core.seproxy.event;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.core.seproxy.SeReader;

/**
 * The ObservableReader interface provides the API for observable readers.
 * <ul>
 * <li>Observer management
 * <li>Start/stop of SE detection
 * <li>Managing the default selection
 * <li>Definition of polling and notification modes
 * </ul>
 */
public interface ObservableReader extends SeReader {
    /**
     * Interface to be implemented by reader observers.
     */
    interface ReaderObserver {
        void update(ReaderEvent event);
    }

    /**
     * The NotificationMode defines the expected behavior when processing a default selection.
     */
    enum NotificationMode {
        /**
         * all SEs presented to readers are notified regardless of the result of the default
         * selection.
         */
        ALWAYS("always"),
        /**
         * only SEs that have been successfully selected (logical channel open) will be notified.
         * The others will be ignored and the application will not be aware of them.
         */
        MATCHED_ONLY("matched_only");

        private String name;

        NotificationMode(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        /**
         * Reverse Lookup Implementation
         * <p>
         * The purpose of the lookup Map and its associated method is to allow the serialization and
         * deserialization of the enum of the notification mode, especially in remote context.
         */

        /**
         * Lookup table
         */
        private static final Map<String, NotificationMode> lookup =
                new HashMap<String, NotificationMode>();

        /**
         * Populating the lookup table on loading time
         */
        static {
            for (NotificationMode env : NotificationMode.values()) {
                lookup.put(env.getName(), env);
            }
        }

        /**
         * This method can be used for reverse lookup purpose
         *
         * @param name the enum name
         * @return the corresponding enum
         */
        public static NotificationMode get(String name) {
            return lookup.get(name);
        }
    }

    /**
     * Indicates the action to be taken after processing a SE.
     */
    enum PollingMode {
        /**
         * continue waiting for the insertion of a next SE.
         */
        REPEATING,
        /**
         * stop and wait for a restart signal.
         */
        SINGLESHOT
    }

    /**
     * Add a reader observer.
     * <p>
     * The observer will receive all the events produced by this reader (card insertion, removal,
     * etc.)
     *
     * @param observer the observer object
     */
    void addObserver(ReaderObserver observer);

    /**
     * Remove a reader observer.
     * <p>
     * The observer will not receive any of the events produced by this reader.
     *
     * @param observer the observer object
     */
    void removeObserver(ReaderObserver observer);

    /**
     * Push a ReaderEvent of the {@link ObservableReader} to its registered observers.
     *
     * @param event the event (see {@link ReaderEvent})
     */
    void notifyObservers(ReaderEvent event);

    /**
     * Remove all observers at once
     */
    void clearObservers();

    /**
     * @return the number of observers
     */
    int countObservers();

    /**
     * Starts the SE detection. Once activated, the application can be notified of the arrival of an
     * SE.
     * 
     * @param pollingMode indicates the action to be followed after processing the SE: if REPEATING,
     *        the SE detection is restarted, if SINGLESHOT, the SE detection is stopped until a new
     *        call to startSeDetection is made.
     */
    void startSeDetection(PollingMode pollingMode);

    /**
     * Stops the SE detection.
     * <p>
     * This method must be overloaded by readers depending on the particularity of their management
     * of the start of SE detection.
     */
    void stopSeDetection();

    /**
     * Defines the selection request to be processed when an SE is inserted. Depending on the SE and
     * the notificationMode parameter, a SE_INSERTED, SE_MATCHED or no event at all will be notified
     * to the application observers.
     * 
     * @param defaultSelectionsRequest the selection request to be operated
     * @param notificationMode indicates whether a SE_INSERTED event should be notified even if the
     *        selection has failed (ALWAYS) or whether the SE insertion should be ignored in this
     *        case (MATCHED_ONLY).
     */
    void setDefaultSelectionRequest(AbstractDefaultSelectionsRequest defaultSelectionsRequest,
            NotificationMode notificationMode);

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
    void setDefaultSelectionRequest(AbstractDefaultSelectionsRequest defaultSelectionsRequest,
            NotificationMode notificationMode, PollingMode pollingMode);

    /**
     * Signal sent by the application to the reader to indicate the end of the application
     * processing.
     * <p>
     * Depending on whether a request with the indication CLOSE_AFTER has been executed before or
     * not, a closing message will be sent to the reader in order to proceed with the closing of the
     * physical channel.
     * <p>
     * The action to be continued will be the one defined by the PollingMode used to start the SE
     * detection.
     * <p>
     * The call of this method is mandatory only if the current transaction performed via an
     * observed reader did not end with a call to an operation causing a physical channel closing
     * (CLOSE_AFTER). The main objective here is to achieve the closing of the physical channel. A
     * typical use of this method is in handling exceptions (catch) that may occur during a
     * transaction that would prevent reaching the last operation closing the physical channel.
     * <p>
     * The channel closing here refers to the application's intention to no longer communicate with
     * the SE. This may take the form of an immediate physical channel closing in the case of an
     * unobserved reader or a delayed closing upon removal of the SE (removal sequence).
     */
    void notifySeProcessed();
}
