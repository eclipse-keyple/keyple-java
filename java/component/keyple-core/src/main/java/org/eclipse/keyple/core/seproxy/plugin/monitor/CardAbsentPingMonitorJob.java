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

import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableLocalReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CardAbsentPingMonitorJob extends AbstractMonitorJob {

    private static final Logger logger = LoggerFactory.getLogger(CardAbsentPingMonitorJob.class);

    AbstractObservableLocalReader reader;

    public CardAbsentPingMonitorJob(AbstractObservableLocalReader reader) {
        this.reader = reader;
    }

    @Override
    public Runnable getMonitorJob() {
        return new Runnable() {
            long counting = 0;
            long threeshold = 200;
            long retries = 0;

            @Override
            public void run() {
                while (true) {
                    logger.debug("[{}] Polling from isSePresentPing", reader.getName());
                    if (!reader.isSePresentPing()) {
                        state.onEvent(AbstractObservableLocalReader.InternalEvent.SE_REMOVED);
                    }
                    retries++;

                    if (logger.isTraceEnabled()) {
                        logger.trace("[{}] Polling retries :{}, time left {} ms", reader.getName(),
                                retries);
                    }
                    try {
                        // wait a bit
                        Thread.sleep(threeshold);
                    } catch (InterruptedException e) {
                    }
                    counting = counting + threeshold;

                }
            }

        };
    }

}
