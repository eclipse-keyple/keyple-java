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
import org.eclipse.keyple.core.util.Observable;

public interface ObservableReader extends SeReader {
    interface ReaderObserver extends Observable.Observer<ReaderEvent> {
    }

    enum NotificationMode {
        ALWAYS("always"), MATCHED_ONLY("matched_only");

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
     * Indicates the action to be taken after processing a SE: continue waiting for the insertion of
     * a next SE (CONTINUE) or stop and wait for a restart signal (STOP).
     */
    enum PollingMode {
        CONTINUE, STOP
    }

    void addObserver(ReaderObserver observer);

    void removeObserver(ReaderObserver observer);

    void notifyObservers(ReaderEvent event);

    void clearObservers();

    /**
     * Starts the SE detection. Once activated, the application can be notified of the arrival of an
     * SE.
     * 
     * @param pollingMode indicates the action to be followed after processing the SE: if CONTINUE,
     *        the SE detection is restarted, if STOP, the SE detection is stopped until a new call
     *        to startSeDetection is made.
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
     */
    void notifySeProcessed();

    int countObservers();
}
