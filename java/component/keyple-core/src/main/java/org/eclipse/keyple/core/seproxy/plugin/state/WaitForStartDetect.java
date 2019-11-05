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
import org.eclipse.keyple.core.seproxy.plugin.monitor.MonitoringJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Wait for Start SE Detection
 */
public class WaitForStartDetect extends AbstractObservableState {

    /** logger */
    private static final Logger logger = LoggerFactory.getLogger(WaitForStartDetect.class);

    public WaitForStartDetect(AbstractObservableLocalReader reader) {
        super(MonitoringState.WAIT_FOR_START_DETECTION, reader);
    }

    public WaitForStartDetect(AbstractObservableLocalReader reader,
                              MonitoringJob monitoringJob, ExecutorService executorService) {
        super(MonitoringState.WAIT_FOR_START_DETECTION, reader, monitoringJob, executorService);
    }

    @Override
    public void onEvent(AbstractObservableLocalReader.InternalEvent event) {
        logger.trace("[{}] onEvent => Event {} received in currentState {}", reader.getName(),
                event, state);

        /*
         * Process InternalEvent
         */
        switch (event) {
            case START_DETECT:
                switchState(MonitoringState.WAIT_FOR_SE_INSERTION);
                break;

            default:
                logger.warn("[{}] Ignore =>  Event {} received in currentState {}",
                        reader.getName(), event, state);
                break;
        }
    }

}
