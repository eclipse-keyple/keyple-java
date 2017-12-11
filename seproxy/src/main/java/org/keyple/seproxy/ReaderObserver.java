package org.keyple.seproxy;

/**
 * An asynchronous update interface for receiving notifications about Reader
 * information as the Reader is constructed.
 *
 * @author Ixxi
 */
public interface ReaderObserver {

    /**
     * This method is called when information about an Reader which was
     * previously requested using an asynchronous interface becomes available.
     *
     * @param event
     *            the event
     */
    void notify(ReaderEvent event);

}