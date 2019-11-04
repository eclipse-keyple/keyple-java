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
package org.eclipse.keyple.core.seproxy.plugin.state;

import java.util.concurrent.ExecutorService;
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableState;
import org.eclipse.keyple.core.seproxy.plugin.monitor.AbstractMonitoringJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitForSeInsertion extends AbstractObservableState {

    /** logger */
    private static final Logger logger = LoggerFactory.getLogger(WaitForSeInsertion.class);

    public WaitForSeInsertion(AbstractObservableLocalReader reader) {
        super(MonitoringState.WAIT_FOR_SE_INSERTION, reader);
    }

    public WaitForSeInsertion(AbstractObservableLocalReader reader,
            AbstractMonitoringJob monitoringJob, ExecutorService executorService) {
        super(MonitoringState.WAIT_FOR_SE_INSERTION, reader, monitoringJob, executorService);
    }

    @Override
    public void onEvent(AbstractObservableLocalReader.InternalEvent event) {
        logger.trace("[{}] onEvent => Event {} received in currentState {}", reader.getName(),
                event, state);
        switch (event) {
            case SE_INSERTED:
                // process default selection if any
                if (this.reader.processSeInserted()) {
                    switchState(MonitoringState.WAIT_FOR_SE_PROCESSING);
                } else {
                    // if none event was sent to the application, back to SE detection
                    // stay in the same state
                    // switchState(MonitoringState.WAIT_FOR_START_DETECTION);
                    logger.trace("[{}] onEvent => Inserted SE hasn't matched" ,
                            reader.getName());
                }
                break;

            case STOP_DETECT:
                switchState(MonitoringState.WAIT_FOR_START_DETECTION);
                break;

            case SE_REMOVED:
                // SE has been removed during default selection
                switchState(MonitoringState.WAIT_FOR_START_DETECTION);
                break;

            default:
                logger.warn("[{}] Ignore =>  Event {} received in currentState {}",
                        reader.getName(), event, state);
                break;
        }
    }

}
