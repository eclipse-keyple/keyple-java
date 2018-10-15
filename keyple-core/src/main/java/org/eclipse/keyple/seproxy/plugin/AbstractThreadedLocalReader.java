/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.seproxy.plugin;

import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract definition of an threader local reader. Factorizes the observation mechanism through the
 * implementation of a monitoring thread.
 */
public abstract class AbstractThreadedLocalReader extends AbstractSelectionLocalReader {

    private static final Logger logger = LoggerFactory.getLogger(AbstractThreadedLocalReader.class);
    private EventThread thread;
    private static final AtomicInteger threadCount = new AtomicInteger();
    /**
     * Thread wait timeout in ms
     */
    protected long threadWaitTimeout;

    protected AbstractThreadedLocalReader(String pluginName, String readerName) {
        super(pluginName, readerName);
        /// create and launch a monitoring thread
        thread = new EventThread(this.getPluginName(), this.getName());
        thread.start();
    }

    /**
     * setter to fix the wait timeout in ms.
     *
     * @param timeout Timeout to use
     */
    protected final void setThreadWaitTimeout(long timeout) {
        this.threadWaitTimeout = timeout;
    }

    /**
     * Waits for a card. Returns true if a card is detected before the end of the provided timeout.
     * Returns false if no card detected within the delay.
     *
     * @param timeout the delay in millisecond we wait for a card insertion
     * @return presence status
     * @throws NoStackTraceThrowable a exception without stack trace in order to be catched and
     *         processed silently
     */
    protected abstract boolean waitForCardPresent(long timeout) throws NoStackTraceThrowable;

    /**
     * Wait until the card disappears. Returns true if a card has disappeared before the end of the
     * provided timeout. Returns false if the is still present within the delay. Closes the physical
     * channel when the card has disappeared.
     *
     * @param timeout the delay in millisecond we wait for a card to be withdrawn
     * @return presence status
     * @throws NoStackTraceThrowable a exception without stack trace in order to be catched and
     *         processed silently
     */
    protected abstract boolean waitForCardAbsent(long timeout) throws NoStackTraceThrowable;

    /**
     * Thread in charge of reporting live events
     */
    private class EventThread extends Thread {
        /**
         * Plugin name
         */
        private final String pluginName;

        /**
         * Reader that we'll report about
         */
        private final String readerName;

        /**
         * If the thread should be kept a alive
         */
        private volatile boolean running = true;

        /**
         * Constructor
         * 
         * @param pluginName name of the plugin that instantiated the reader
         * @param readerName name of the reader who owns this thread
         */
        EventThread(String pluginName, String readerName) {
            super("observable-reader-events-" + threadCount.addAndGet(1));
            setDaemon(true);
            this.pluginName = pluginName;
            this.readerName = readerName;
        }

        /**
         * Marks the thread as one that should end when the last cardWaitTimeout occurs
         */
        void end() {
            running = false;
            this.interrupt(); // exit io wait if needed
        }

        private void cardRemoved() {
            notifyObservers(new ReaderEvent(this.pluginName, this.readerName,
                    ReaderEvent.EventType.SE_REMOVAL));
        }

        private void cardInserted() {
            notifyObservers(new ReaderEvent(this.pluginName, this.readerName,
                    ReaderEvent.EventType.SE_INSERTED));
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
            } catch (NoStackTraceThrowable e) {
                logger.trace("[{}] Exception occurred in monitoring thread: {}", readerName,
                        e.getMessage());
            }
        }
    }

    /**
     * Called when the class is unloaded. Attempt to do a clean exit.
     * 
     * @throws Throwable a generic exception
     */
    @Override
    protected void finalize() throws Throwable {
        thread.end();
        thread = null;
        logger.trace("[{}] Observable Reader thread ended.", this.getName());
        super.finalize();
    }
}
