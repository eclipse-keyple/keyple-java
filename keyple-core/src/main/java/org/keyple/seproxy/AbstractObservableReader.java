/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;


import org.keyple.util.Observable;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

/**
 * Abstract definition of an observable reader.
 */
public abstract class AbstractObservableReader extends Observable<ReaderEvent>
        implements ProxyReader {

    private static final ILogger logger = SLoggerFactory.getLogger(AbstractObservableReader.class);

    /**
     * Add an observer to a terminal reader.
     * 
     * This will allow to be notified about all card insertion/removal events. Please note that you
     * shouldn't reuse the notification threads for your card processing logic.
     *
     * @param observer Observer to notify
     */
    public void addObserver(Observable.Observer<? super ReaderEvent> observer) {
        logger.info("AbstractObservableReader: Adding an observer", "action",
                "observable_reader.add_observer", "readerName", getName());
        super.addObserver(observer);
    }

    /**
     * Remove an observer from a terminal reader.
     *
     * @param observer Observer to stop notifying
     */
    public void removeObserver(Observable.Observer<? super ReaderEvent> observer) {
        logger.info("AbstractObservableReader: Deleting an observer", "action",
                "observable_reader.delete_observer", "readerName", getName());
        super.removeObserver(observer);
    }

    /**
     * This method shall be called only from a SE Proxy plugin by a reader implementing
     * AbstractObservableReader
     * 
     * push a ReaderEvent of the selected AbstractObservableReader to its registered ReaderObserver.
     *
     * @param event the event
     */
    public final void notifyObservers(ReaderEvent event) {
        logger.info("AbstractObservableReader: Notifying of an event", "action",
                "observable_reader.notify_observers", "event", event, "readerName", getName());
        setChanged();
        super.notifyObservers(event);
    }

}
