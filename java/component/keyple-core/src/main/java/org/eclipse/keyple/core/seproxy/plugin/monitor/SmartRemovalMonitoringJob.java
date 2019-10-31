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
package org.eclipse.keyple.core.seproxy.plugin.monitor;

import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.SmartRemovalReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartRemovalMonitoringJob extends AbstractMonitoringJob {

    private static final Logger logger = LoggerFactory.getLogger(SmartRemovalMonitoringJob.class);

    SmartRemovalReader reader;

    public SmartRemovalMonitoringJob(SmartRemovalReader reader) {
        this.reader = reader;
    }

    @Override
    public Runnable getMonitoringJob() {
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
