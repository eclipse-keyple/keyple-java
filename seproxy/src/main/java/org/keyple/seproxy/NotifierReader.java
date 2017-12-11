package org.keyple.seproxy;

public abstract class NotifierReader extends ObservableReader implements ProxyReader{

    /**
     * push a ReaderEvent of the selected ObservableReader to its registered ReaderObserver.
     *
     * @param event
     *            the event
     */
    public final void notifyObservers(ReaderEvent event){
        super.notifyObservers(event);
    }


}
