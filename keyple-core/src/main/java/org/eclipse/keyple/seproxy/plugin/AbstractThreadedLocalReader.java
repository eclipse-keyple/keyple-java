/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy.plugin;

import java.util.concurrent.atomic.AtomicInteger;
import javax.smartcardio.CardException;
import org.eclipse.keyple.seproxy.event.AbstractObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

/**
 * Abstract definition of an threader local reader. Factorizes the observation mechanism through the
 * implementation of a monitoring thread.
 */
public abstract class AbstractThreadedLocalReader extends AbstractLocalReader {

    private static final ILogger logger = SLoggerFactory.getLogger(AbstractLocalReader.class);
    private EventThread thread;
    private static final AtomicInteger threadCount = new AtomicInteger();
    /**
     * Thread wait timeout in ms
     */
    protected long threadWaitTimeout;

    /**
     * setter to fix the wait timeout in ms.
     *
     * @param timeout Timeout to use
     * @return Current instance
     */
    public final void setThreadWaitTimeout(long timeout) {
        this.threadWaitTimeout = timeout;
    }

    /**
     * Adds an observer to the reader event.
     * 
     * @param observer Observer to notify
     */
    @Override
    public final void addObserver(Observer observer) {
        // We don't need synchronization for the list itself, we need to make sure we're not
        // starting and closing the thread at the same time.
        synchronized (observers) {
            super.addObserver(observer);
            if (observers.size() == 1) {
                if (thread != null) { // <-- This should never happen and can probably be dropped at
                    // some point
                    throw new IllegalStateException("The reader thread shouldn't null");
                }

                thread = new EventThread(this);
                thread.start();
            }
        }
    }

    @Override
    public final void removeObserver(Observer observer) {
        synchronized (observers) {
            super.removeObserver(observer);
            if (observers.isEmpty()) {
                if (thread == null) { // <-- This should never happen and can probably be dropped at
                    // some point
                    throw new IllegalStateException("The reader thread should be null");
                }

                // We'll let the thread calmly end its course after the waitForCard(Absent|Present)
                // timeout occurs
                thread.end();
                thread = null;
            }
        }
    }

    /**
     * Waits for a card. Returns true if a card is detected before the end of the provided timeout.
     * Returns false if no card detected within the delay.
     *
     * @param timeout
     * @return presence status
     */
    public abstract boolean waitForCardPresent(long timeout) throws CardException;

    /**
     * Wait until the card disappears. Returns true if a card has disappeared before the end of the
     * provided timeout. Returns false if the is still present within the delay. Closes the physical
     * channel when the card has disappeared.
     *
     * @param timeout
     * @return presence status
     */
    public abstract boolean waitForCardAbsent(long timeout) throws CardException, IOReaderException;

    /**
     * Thread in charge of reporting live events
     */
    class EventThread extends Thread {
        /**
         * Reader that we'll report about
         */
        private final AbstractObservableReader reader;

        /**
         * If the thread should be kept a alive
         */
        private volatile boolean running = true;
        private long threadWaitTimeout;

        /**
         * Constructor
         *
         * @param reader AbstractObservableReader
         */
        EventThread(AbstractObservableReader reader) {
            super("observable-reader-events-" + threadCount.addAndGet(1));
            setDaemon(true);
            this.reader = reader;
        }

        /**
         * Marks the thread as one that should end when the last cardWaitTimeout occurs
         */
        void end() {
            running = false;
        }

        private void cardRemoved() {
            notifyObservers(ReaderEvent.SE_REMOVAL);
        }

        private void cardInserted() {
            notifyObservers(ReaderEvent.SE_INSERTED);
        }

        /**
         * Event failed
         *
         * @param ex Exception
         */
        private void exceptionThrown(Exception ex) {
            logger.error("Observable Reader: Error handling events", "action",
                    "observable_reader.event_error", "readerName", getName(), "exception", ex);
            if (ex instanceof CardException || ex instanceof IOReaderException) {
                notifyObservers(ReaderEvent.IO_ERROR);
            }
        }

        public void run() {
            try {
                // First thing we'll do is to notify that a card was inserted if one is already
                // present.
                if (isSePresent()) {
                    cardInserted();
                }

                while (running) {
                    // If we have a card,
                    if (isSePresent()) {
                        // we will wait for it to disappear
                        if (waitForCardAbsent(threadWaitTimeout)) {
                            // and notify about it.
                            cardRemoved();
                        }
                        // false means timeout, and we go back to the beginning of the loop
                    }
                    // If we don't,
                    else {
                        // we will wait for it to appear
                        if (waitForCardPresent(threadWaitTimeout)) {
                            cardInserted();
                        }
                        // false means timeout, and we go back to the beginning of the loop
                    }
                }
            } catch (Exception e) {
                exceptionThrown(e);
            }
        }
    }
}
