/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.core.seproxy.plugin;

import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.core.seproxy.exception.NoStackTraceThrowable;
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
    protected boolean waitForRemovalModeEnabled = false;
    /**
     * Thread wait timeout in ms
     */
    protected long threadWaitTimeout;

    protected AbstractThreadedLocalReader(String pluginName, String readerName) {
        super(pluginName, readerName);
    }

    /**
     * Start the monitoring thread.
     * <p>
     * The thread is created if it does not already exist
     */
    @Override
    protected void startObservation() {
        thread = new EventThread(this.getPluginName(), this.getName());
        thread.start();
    }

    /**
     * Terminate the monitoring thread
     */
    @Override
    protected void stopObservation() {
        if (thread != null) {
            thread.end();
        }
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
     * <p>
     * This method must be implemented by the plugin's reader class.
     * <p>
     * Returns false if no card detected within the delay.
     *
     * @param timeout the delay in millisecond we wait for a card insertion, a value of zero means
     *        wait for ever.
     * @return presence status
     * @throws NoStackTraceThrowable a exception without stack trace in order to be catched and
     *         processed silently
     */
    protected abstract boolean waitForCardPresent(long timeout) throws NoStackTraceThrowable;



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

        public void run() {
            try {
                // First thing we'll do is to notify that a card was inserted if one is already
                // present.
                if (isSePresent()) {
                    logger.trace("[{}] Card is already present in reader", readerName);
                    cardInserted();
                }

                if (this instanceof SmartReader) {
                    logger.debug("This READER is instanceof SmartReader");
                } else {
                    logger.debug("This READER is NOT instanceof SmartReader");
                }

                while (running) {
                    logger.trace("[{}] observe card insertion", readerName);
                    // we will wait for it to appear
                    if (waitForCardPresent(0)) {
                        // notify insertion
                        cardInserted();
                        if (waitForRemovalModeEnabled) {
                            // wait as long as the PO responds (timeout is useless)
                            logger.trace("[{}] Observe card removal", readerName);
                            if (this instanceof SmartReader) {
                                ((SmartReader) this).waitForCardAbsentNative(0);
                            } else {
                                waitForCardAbsentPing(0);
                            }
                        }
                        // notify removal
                        cardRemoved();
                    }
                }
            } catch (NoStackTraceThrowable e) {
                logger.trace("[{}] Exception occurred in monitoring thread: {}", readerName,
                        e.getMessage());
            }
        }
    }

    /**
     * Wait for the card to disappear.
     * <p>
     * The method used to do this is to replay, while the physical channel is still open, the
     * request that made the current selection until the PO no longer responds.
     *
     * @param timeout the delay in millisecond we wait for a card insertion, a value of zero means
     *        wait for ever.
     */
    private void waitForCardAbsentPing(int timeout) {
        /*
         * TODO remove lastSuccessfulSelector if (lastSuccessfulSelector != null) { while (true) {
         * try { SelectionStatus selectionStatus = openLogicalChannel(lastSuccessfulSelector); if
         * (selectionStatus != null && selectionStatus.hasMatched()) { // avoid Thread.sleep(10); }
         * else return; } catch (KeypleIOReaderException ex) { // considered as a card removal
         * return; } catch (KeypleChannelStateException e) { // considered as a card removal return;
         * } catch (KeypleApplicationSelectionException e) { // considered as a card removal return;
         * } catch (InterruptedException e) { e.printStackTrace(); } } }
         */
        // APDU sent to check the communication with the PO
        // byte[] apdu = new byte[] {(byte) 0x00, (byte) 0xCA, (byte) 0x00, (byte) 0x6F};
        byte[] apdu = new byte[] {(byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00};
        // byte[] apdu = new byte[]{(byte)0x00, (byte)0xB2, (byte)0x00, (byte)0x00};
        // loop for ever until the PO stop responding
        try {
            while (true) {
                byte[] rapdu = new byte[0];
                rapdu = transmitApdu(apdu);
                // sleep a little to reduce the cpu consumption of the current thread
                Thread.sleep(50);
            }
        } catch (KeypleIOReaderException e) {
            // log only unexpected exceptions, else exit silently
            logger.trace("[{}] Exception occured in waitForCardAbsentPing. Message: {}",
                    this.getName(), e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.debug("Card removed.");
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
