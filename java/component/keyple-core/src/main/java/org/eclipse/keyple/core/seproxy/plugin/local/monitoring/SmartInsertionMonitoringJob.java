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

import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.plugin.local.AbstractObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.local.AbstractObservableState;
import org.eclipse.keyple.core.seproxy.plugin.local.MonitoringJob;
import org.eclipse.keyple.core.seproxy.plugin.local.SmartInsertionReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detect the SE insertion thanks to the method {@link SmartInsertionReader#waitForCardPresent()}.
 * This method is invoked in another thread.
 * <p>
 * The job waits indefinitely for the waitForCardPresent method to return.
 * <p>
 * When an SE is present, an internal SE_INSERTED event is fired.
 * <p>
 * If a communication problem with the reader occurs (KeypleReaderIOException) an internal
 * STOP_DETECT event is fired.
 */
public class SmartInsertionMonitoringJob implements MonitoringJob {

    private static final Logger logger = LoggerFactory.getLogger(SmartInsertionMonitoringJob.class);

    private final SmartInsertionReader reader;

    public SmartInsertionMonitoringJob(SmartInsertionReader reader) {
        this.reader = reader;
    }



    @Override
    public Runnable getMonitoringJob(final AbstractObservableState state) {
        /**
         * Invoke the method SmartInsertionReader#waitForCardPresent() in another thread
         */
        return new Runnable() {
            @Override
            public void run() {
                logger.trace("[{}] Invoke waitForCardPresent asynchronously", reader.getName());
                try {
                    if (reader.waitForCardPresent()) {
                        state.onEvent(AbstractObservableLocalReader.InternalEvent.SE_INSERTED);
                    }
                } catch (KeypleReaderIOException e) {
                    logger.trace(
                            "[{}] waitForCardPresent => Error while polling SE with waitForCardPresent",
                            reader.getName());
                    state.onEvent(AbstractObservableLocalReader.InternalEvent.STOP_DETECT);
                }
            }
        };
    }

    @Override
    public void stop() {
        logger.trace("[{}] stopWaitForCard on reader", reader.getName());
        reader.stopWaitForCard();
    }

}
