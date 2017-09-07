package org.keyple.seproxy;

/**
 * The Interface ObservableReader. In order to notify a ticketing application in
 * case of specific reader events, the SE Proxy implements the ‘Observer’ design
 * pattern. The ObservableReader object is optionally proposed by plugins for
 * readers able to notify events in case of IO Error, SE Insertion or removal.
 * 
 * @author Ixxi
 */
public abstract class ObservableReader implements ProxyReader{

	/**
	 * 
	 *add a ReaderObserver to the list of registered ReaderObserver for the selected ObservableReader.
	 * @param calledBack
	 *            the called back
	 */
	abstract void attachObserver(ReaderObserver calledBack);

	/**
	 * remove a ReaderObserver from the list of registered ReaderObserver for the selected
ObservableReader.
	 *
	 * @param calledback
	 *            the calledback
	 */
	abstract void detachObserver(ReaderObserver calledback);

	/**
	 * push a ReaderEvent of the selected ObservableReader to its registered ReaderObserver.
	 *
	 * @param event
	 *            the event
	 */
	abstract void notifyObservers(ReaderEvent event);

}