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

public final class PluginEvent {
    /**
     * The name of the plugin handling the reader that produced the event
     */
    private final String pluginName;

    /**
     * The name of the reader involved
     */
    private final String readerName;

    /**
     * The type of event
     */
    private final EventType eventType;

    /**
     * The different types of reader event
     */
    public enum EventType {
        /**
         * A reader has been connected.
         */
        READER_CONNECTED("Reader connected"),

        /**
         * A reader has been disconnected.
         */
        READER_DISCONNECTED("Reader disconnected");

        /** The event name. */
        private String name;

        EventType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public PluginEvent(String pluginName, String readerName, EventType eventType) {
        this.pluginName = pluginName;
        this.readerName = readerName;
        this.eventType = eventType;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getReaderName() {
        return readerName;
    }

    public EventType getEventType() {
        return eventType;
    }
}
