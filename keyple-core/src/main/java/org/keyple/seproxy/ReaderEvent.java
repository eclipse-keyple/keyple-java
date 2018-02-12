/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

/**
 * The Class ReaderEvent. This class is used to notify an event of a specific ObservableReader to
 * its registered ReaderObserver in case of IO Error, SE insertion or removal.
 *
 * @author Ixxi
 */
public class ReaderEvent {

    /**
     * The Enum EventType. defined with the elements: ‘IOError’, ‘SEInserted’ and ‘SERemoval’.
     */
    public enum EventType {

        /** The io error. */
        IO_ERROR,
        /** The se inserted. */
        SE_INSERTED,
        /** The se removal. */
        SE_REMOVAL
    }

    /** the reader pushing the notification. */
    private ObservableReader reader;

    /** the type of the notified event. */
    private EventType event;

    /**
     * Instantiates a new reader event.
     *
     * @param reader the reader
     * @param event the event
     */
    public ReaderEvent(ObservableReader reader, EventType event) {
        this.reader = reader;
        this.event = event;
    }

    /**
     * Gets the reader.
     *
     * @return the reader notifying the event.
     */
    public ObservableReader getReader() {
        return reader;
    }

    /**
     * Gets the event.
     *
     * @return the type of the event.
     */
    public EventType getEventType() {
        return event;
    }

}
