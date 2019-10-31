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
import org.eclipse.keyple.core.seproxy.plugin.SmartInsertionReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartInsertionMonitorJob extends AbstractMonitorJob {

    private static final Logger logger = LoggerFactory.getLogger(SmartInsertionMonitorJob.class);

    SmartInsertionReader reader;

    public SmartInsertionMonitorJob(SmartInsertionReader reader) {
        this.reader = reader;
    }

    @Override
    public Runnable getMonitorJob() {
        return new Runnable() {
            @Override
            public void run() {
                logger.trace("[{}] Invoke waitForCardPresent asynchronously", reader.getName());
                try {
                    if (reader.waitForCardPresent()) {
                        state.onEvent(AbstractObservableLocalReader.InternalEvent.SE_INSERTED);
                    }
                } catch (KeypleIOReaderException e) {
                    logger.trace(
                            "[{}] waitForCardPresent => Error while polling card with waitForCardPresent",
                            reader.getName());
                    state.onEvent(AbstractObservableLocalReader.InternalEvent.STOP_DETECT);
                }
            }
        };
    }

}