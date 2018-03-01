/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The Interface ObservableReader. In order to notify a ticketing application in case of specific
 * reader events, the SE Proxy implements the ‘Observer’ design pattern. The ObservableReader object
 * is optionally proposed by plugins for readers able to notify events in case of IO Error, SE
 * Insertion or removal.
 *
 * @author Ixxi
 */
public abstract class ObservableReader implements ProxyReader {

    // TODO: Drop this implementation, it doesn't respect the java's definition of it (it missed the
    // change handling)
    // and if we redefine it, we might as well make it a generic one, which is done in the
    // "feature-example-common" branch

    /**
     * an array referencing the registered ReaderObserver of the Reader.
     */
    protected final List<ReaderObserver> readerObservers =
            new CopyOnWriteArrayList<ReaderObserver>();

    /**
     * This method shall be called only from a terminal application implementing ObservableReader
     * 
     * add a ReaderObserver to the list of registered ReaderObserver for the selected
     * ObservableReader.
     *
     * @param calledBack the called back
     */
    public void addObserver(ReaderObserver calledBack) {
        readerObservers.add(calledBack);
    }

    /**
     * This method shall be called only from a terminal application implementing ObservableReader
     * 
     * remove a ReaderObserver from the list of registered ReaderObserver for the selected
     * ObservableReader.
     *
     * @param calledback the calledback
     */
    public void deleteObserver(ReaderObserver calledback) {
        readerObservers.remove(calledback);
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
        for (ReaderObserver observer : readerObservers) {
            observer.notify(event);
        }
    }

}
