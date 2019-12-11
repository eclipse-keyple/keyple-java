/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.core.seproxy.plugin.local.monitoring;

import org.eclipse.keyple.core.seproxy.plugin.local.AbstractObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.local.AbstractObservableState;
import org.eclipse.keyple.core.seproxy.plugin.local.MonitoringJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ping the SE to detect removal thanks to the method
 * {@link AbstractObservableLocalReader#isSePresentPing()}. This method is invoked in another
 * thread.
 * <p>
 * This job should be used by readers who do not have the ability to natively detect the
 * disappearance of the SE at the end of the transaction.
 * <p>
 * It is based on sending a neutral APDU command as long as the SE is responding, an internal
 * SE_REMOVED event is fired when the SE is no longer responding.
 * <p>
 * By default a delay of 200 ms is inserted between each APDU sending .
 */
public class CardAbsentPingMonitoringJob implements MonitoringJob {

    private static final Logger logger = LoggerFactory.getLogger(CardAbsentPingMonitoringJob.class);

    private final AbstractObservableLocalReader reader;
    private Runnable job;
    Boolean loop;
    private long removalWait = 200;

    /**
     * Create a job monitor job that ping the SE with the method isSePresentPing()
     * @param reader : reference to the reader
     */
    public CardAbsentPingMonitoringJob(AbstractObservableLocalReader reader) {
        this.reader = reader;
        this.loop = true;
    }

    /**
     * Create a job monitor job that ping the SE with the method isSePresentPing()
     * @param reader : reference to the reader
     * @param removalWait : delay between between each APDU sending
     */
    public CardAbsentPingMonitoringJob(AbstractObservableLocalReader reader, long removalWait) {
        this.reader = reader;
        this.loop = true;
        this.removalWait = removalWait;
    }

    @Override
    public Runnable getMonitoringJob(final AbstractObservableState state) {

        /**
         * Loop until one the following condition is met : -
         * AbstractObservableLocalReader#isSePresentPing returns false, meaning that the SE ping has
         * failed - InterruptedException is caught
         */
        job = new Runnable() {
            long retries = 0;

            @Override
            public void run() {
                logger.debug("[{}] Polling from isSePresentPing", reader.getName());
                while (loop) {
                    if (!reader.isSePresentPing()) {
                        logger.debug("[{}] The SE stopped responding", reader.getName());
                        loop = false;
                        state.onEvent(AbstractObservableLocalReader.InternalEvent.SE_REMOVED);
                        return;
                    }
                    retries++;

                    if (logger.isTraceEnabled()) {
                        logger.trace("[{}] Polling retries : {}", reader.getName(), retries);
                    }
                    try {
                        // wait a bit
                        Thread.sleep(removalWait);
                    } catch (InterruptedException ignored) {
                        // Restore interrupted state...      
                        Thread.currentThread().interrupt();
                        loop = false;
                    }
                }

                logger.debug("[{}] Polling loop has been stopped", reader.getName());

            }
        };
        return job;
    }

    @Override
    public void stop() {
        logger.debug("[{}] Stop Polling ", reader.getName());
        loop = false;
    }
}
