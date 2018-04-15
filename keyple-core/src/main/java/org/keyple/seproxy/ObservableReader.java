/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;


import org.keyple.util.event.Observable;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

/**
 * The Interface ObservableReader. In order to notify a ticketing application in case of specific
 * reader events, the SE Proxy implements the ‘Observer’ design pattern. The ObservableReader object
 * is optionally proposed by plugins for readers able to notify events in case of IO Error, SE
 * Insertion or removal.
 *
 * @author Ixxi
 */
public abstract class ObservableReader extends Observable<ReaderEvent> implements ProxyReader {

    private static final ILogger logger = SLoggerFactory.getLogger(ObservableReader.class);

    // TODO: Drop this implementation, it doesn't respect the java's definition of it (it missed the
    // change handling)
    // and if we redefine it, we might as well make it a generic one, which is done in the
    // "feature-example-common" branch

    /**
     * an array referencing the registered ReaderObserver of the Reader.
     */
    // protected final List<ReaderObserver> readerObservers = new
    // CopyOnWriteArrayList<ReaderObserver>();

    /**
     * Add an observer to a terminal reader.
     * 
     * This will allow to be notified about all card insertion/removal events. Please note that you
     * shouldn't reuse the notification threads for your card processing logic.
     *
     * @param observer Observer to notify
     */
    public void addObserver(ReaderObserver observer) {
        logger.info("ObservableReader: Adding an observer", "action",
                "observable_reader.add_observer", "readerName", getName());
        super.addObserver(observer);
        // readerObservers.add(observer);
    }

    /**
     * Remove an observer from a terminal reader.
     *
     * @param observer Observer to stop notifying
     */
    public void removeObserver(ReaderObserver observer) {
        logger.info("ObservableReader: Deleting an observer", "action",
                "observable_reader.delete_observer", "readerName", getName());
        // readerObservers.remove(observer);
        super.removeObserver(observer);
    }

    /**
     * This method shall be called only from a SE Proxy plugin by a reader implementing
     * ObservableReader
     * 
     * push a ReaderEvent of the selected ObservableReader to its registered ReaderObserver.
     *
     * @param event the event
     */
    public final void notifyObservers(ReaderEvent event) {
        logger.info("ObservableReader: Notifying of an event", "action",
                "observable_reader.notify_observers", "event", event, "readerName", getName());
        setChanged();
        super.notifyObservers(event);
    }

}
