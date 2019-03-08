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
package org.eclipse.keyple.seproxy.event;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.seproxy.SeReader;
import org.eclipse.keyple.util.Observable;

public interface ObservableReader extends SeReader {
    interface ReaderObserver extends Observable.Observer<ReaderEvent> {
    }

    public enum NotificationMode {
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
         */
        public static NotificationMode get(String name) {
            return lookup.get(name);
        }
    }

    void addObserver(ReaderObserver observer);

    void removeObserver(ReaderObserver observer);

    void notifyObservers(ReaderEvent event);

    void setDefaultSelectionRequest(DefaultSelectionRequest defaultSelectionRequest,
            NotificationMode notificationMode);
}
