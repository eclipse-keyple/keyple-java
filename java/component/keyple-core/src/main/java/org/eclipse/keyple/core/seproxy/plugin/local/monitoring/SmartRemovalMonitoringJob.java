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

import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.core.seproxy.plugin.local.AbstractObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.local.AbstractObservableState;
import org.eclipse.keyple.core.seproxy.plugin.local.MonitoringJob;
import org.eclipse.keyple.core.seproxy.plugin.local.SmartRemovalReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detect the SE removal thanks to the method {@link SmartRemovalReader#waitForCardAbsentNative()}.
 * This method is invoked in another thread
 */
public class SmartRemovalMonitoringJob implements MonitoringJob {

    private static final Logger logger = LoggerFactory.getLogger(SmartRemovalMonitoringJob.class);

    private final SmartRemovalReader reader;

    public SmartRemovalMonitoringJob(SmartRemovalReader reader) {
        this.reader = reader;
    }

    @Override
    public Runnable getMonitoringJob(final AbstractObservableState state) {
        /**
         * Invoke the method SmartRemovalReader#waitForCardAbsentNative() in another thread
         */
        return new Runnable() {
            @Override
            public void run() {
                try {
                    if (reader.waitForCardAbsentNative()) {
                        // timeout is already managed within the task
                        state.onEvent(AbstractObservableLocalReader.InternalEvent.SE_REMOVED);
                    } else {
                        // se was not removed within timeout
                        // onEvent(AbstractObservableLocalReader.InternalEvent.TIME_OUT);
                        logger.trace(
                                "[{}] waitForCardAbsentNative => return false, task interrupted",
                                reader.getName());
                    }
                } catch (KeypleIOReaderException e) {
                    logger.trace(
                            "[{}] waitForCardAbsent => Error while polling card with waitForCardAbsent",
                            reader.getName());
                    state.onEvent(AbstractObservableLocalReader.InternalEvent.STOP_DETECT);
                }
            }
        };
    }

}
