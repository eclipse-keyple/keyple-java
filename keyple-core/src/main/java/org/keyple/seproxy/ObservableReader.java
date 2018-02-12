/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import java.util.ArrayList;
import java.util.List;

/**
 * The Interface ObservableReader. In order to notify a ticketing application in case of specific
 * reader events, the SE Proxy implements the ‘Observer’ design pattern. The ObservableReader object
 * is optionally proposed by plugins for readers able to notify events in case of IO Error, SE
 * Insertion or removal.
 *
 * @author Ixxi
 */
public abstract class ObservableReader implements ProxyReader {

    /**
     * an array referencing the registered ReaderObserver of the Reader.
     */
    private List<ReaderObserver> readerObservers = new ArrayList<ReaderObserver>();

    /**
     * This method shall be called only from a terminal application implementing ObservableReader
     * 
     * add a ReaderObserver to the list of registered ReaderObserver for the selected
     * ObservableReader.
     *
     * @param calledBack the called back
     */
    public final void attachObserver(ReaderObserver calledBack) {
        this.readerObservers.add(calledBack);
    }

    /**
     * This method shall be called only from a terminal application implementing ObservableReader
     * 
     * remove a ReaderObserver from the list of registered ReaderObserver for the selected
     * ObservableReader.
     *
     * @param calledback the calledback
     */
    public final void detachObserver(ReaderObserver calledback) {
        this.readerObservers.remove(calledback);
    }

    // /**
    // * push a ReaderEvent of the selected ObservableReader to its registered
    // * ReaderObserver.
    // *
    // * @param event
    // * the event
    // */
    // protected void notifyObservers(ReaderEvent event) {
    // synchronized (this.readerObservers) {
    // for (ReaderObserver observer : this.readerObservers) {
    // observer.notify(event);
    // }
    // }
    // }

    /**
     * This method shall be called only from a SE Proxy plugin by a reader implementing
     * ObservableReader
     * 
     * push a ReaderEvent of the selected ObservableReader to its registered ReaderObserver.
     *
     * @param event the event
     */
    public final void notifyObservers(ReaderEvent event) {
        synchronized (readerObservers) { // TODO Ixxi a mis un verrou sans l'expliquer, s'agit de
                                         // s'assurer que la liste des observer n'évolue pas
                                         // lorsqu'on la parcourt?
            for (ReaderObserver observer : readerObservers) {
                observer.notify(event);
            }
        }
    }

}
