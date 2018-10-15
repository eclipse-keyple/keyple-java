/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.seproxy.event;


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
     * ReaderEvent constructor
     * 
     * @param pluginName the name of the current plugin
     * @param readerName the name of the current reader
     * @param eventType the type of event
     */
    public ReaderEvent(String pluginName, String readerName, EventType eventType) {
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
