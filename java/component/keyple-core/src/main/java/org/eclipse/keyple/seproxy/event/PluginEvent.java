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

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A {@link PluginEvent} is used to propagate a change of reader state in reader plugin.
 * <p>
 * The getReaderNames and getEventType methods allow the event recipient to retrieve the names of
 * the readers involved and the type of the event.
 * <p>
 * At the moment, two types of events are supported: a connection or disconnection of the reader.
 * <p>
 * Since the event provides a list of reader names, a single event can be used to notify a change
 * for one or more readers.
 * <p>
 * However, only one type of event is notified at a time.
 */
public final class PluginEvent {
    /**
     * The name of the plugin handling the reader that produced the event
     */
    private final String pluginName;

    /**
     * The name of the readers involved
     */
    private SortedSet<String> readerNames = new TreeSet<String>();

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

    /**
     * The type of event
     */
    private final EventType eventType;

    /**
     * Create a PluginEvent for a single reader
     *
     * @param pluginName name of the plugin
     * @param readerName name of the reader
     * @param eventType type of the event, connection or disconnection
     */
    public PluginEvent(String pluginName, String readerName, EventType eventType) {
        this.pluginName = pluginName;
        this.readerNames.add(readerName);
        this.eventType = eventType;
    }

    /**
     * Create a PluginEvent for multiple readers
     *
     * @param pluginName name of the plugin
     * @param readerNames list of reader names
     * @param eventType type of the event, connection or disconnection
     */
    public PluginEvent(String pluginName, Set<String> readerNames, EventType eventType) {
        this.pluginName = pluginName;
        this.readerNames.addAll(readerNames);
        this.eventType = eventType;
    }

    public String getPluginName() {
        return pluginName;
    }

    public SortedSet<String> getReaderNames() {
        return readerNames;
    }

    public EventType getEventType() {
        return eventType;
    }
}
