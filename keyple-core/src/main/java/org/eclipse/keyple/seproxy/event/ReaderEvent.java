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


import org.eclipse.keyple.seproxy.SeResponseSet;

/**
 * ReaderEvent used to notify changes at reader level
 */
public final class ReaderEvent {
    /**
     * The name of the plugin handling the reader that produced the event
     */
    private final String pluginName;

    /**
     * The name of the reader that produced the event
     */
    private final String readerName;

    /**
     * The type of event
     */
    private final EventType eventType;

    /**
     * The response to the selection request
     */
    private final SeResponseSet defaultResponseSet;

    /**
     * The different types of reader event
     */
    public enum EventType {
        /**
         * An io error occurred.
         */
        IO_ERROR("SE Reader IO Error"),

        /**
         * A SE has been inserted.
         */
        SE_INSERTED("SE insertion"),

        /**
         * A SE has been inserted and the default requests process has been operated.
         */
        SE_MATCHED("SE matched"),

        /**
         * The SE has been removed.
         */
        SE_REMOVAL("SE removal");

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
     * ReaderEvent constructor for simple insertion notification mode
     * 
     * @param pluginName the name of the current plugin
     * @param readerName the name of the current reader
     * @param eventType the type of event
     */
    public ReaderEvent(String pluginName, String readerName, EventType eventType) {
        this.pluginName = pluginName;
        this.readerName = readerName;
        this.eventType = eventType;
        this.defaultResponseSet = null;
    }

    /**
     * ReaderEvent constructor for implicit selection notification mode.
     * <p>
     * The event type is fixed to SE_SELECTED.
     *
     * @param pluginName the name of the current plugin
     * @param readerName the name of the current reader
     * @param seResponseSet the response to the default SeRequestSet
     */
    public ReaderEvent(String pluginName, String readerName, SeResponseSet seResponseSet) {
        this.pluginName = pluginName;
        this.readerName = readerName;
        this.eventType = EventType.SE_MATCHED;
        this.defaultResponseSet = seResponseSet;
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

    public SeResponseSet getDefaultResponseSet() {
        return defaultResponseSet;
    }
}
