package org.keyple.seproxy;

import java.util.ArrayList;
import java.util.List;

/**
 * The Interface ObservableReader. In order to notify a ticketing application in
 * case of specific reader events, the SE Proxy implements the ‘Observer’ design
 * pattern. The ObservableReader object is optionally proposed by plugins for
 * readers able to notify events in case of IO Error, SE Insertion or removal.
 *
 * @author Ixxi
 */
public abstract class ObservableReader implements ProxyReader {

    /**
     * an array referencing the registered ReaderObserver of the Reader.
     */
    private List<ReaderObserver> readerObservers = new ArrayList<ReaderObserver>();

    /**
     *
     * add a ReaderObserver to the list of registered ReaderObserver for the
     * selected ObservableReader.
     *
     * @param calledBack
     *            the called back
     */
    public final void attachObserver(ReaderObserver calledBack) {
        this.readerObservers.add(calledBack);
    }

    /**
     * remove a ReaderObserver from the list of registered ReaderObserver for
     * the selected ObservableReader.
     *
     * @param calledback
     *            the calledback
     */
    public final void detachObserver(ReaderObserver calledback) {
        this.readerObservers.remove(calledback);
    }

    /**
     * push a ReaderEvent of the selected ObservableReader to its registered
     * ReaderObserver.
     *
     * @param event
     *            the event
     */
    protected void notifyObservers(ReaderEvent event) {
        synchronized (this.readerObservers) {
            for (ReaderObserver observer : this.readerObservers) {
                observer.notify(event);
            }
        }
    }

}