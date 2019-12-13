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
package org.eclipse.keyple.core.seproxy.plugin.local.state;

import java.util.concurrent.ExecutorService;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.plugin.local.AbstractObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.local.AbstractObservableState;
import org.eclipse.keyple.core.seproxy.plugin.local.MonitoringJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Wait for Se Removal State
 * <p>
 * The state in which the SE is still present and awaiting removal.
 * <ul>
 * <li>Upon SE_REMOVED event, the machine changes state for WAIT_FOR_SE_INSERTION or
 * WAIT_FOR_SE_DETECTION according to the {@link ObservableReader.PollingMode} setting.
 * <li>Upon STOP_DETECT event, the machine changes state for WAIT_FOR_SE_DETECTION.
 * </ul>
 */
public class WaitForSeRemoval extends AbstractObservableState {

    /** logger */
    private static final Logger logger = LoggerFactory.getLogger(WaitForSeRemoval.class);

    public WaitForSeRemoval(AbstractObservableLocalReader reader) {
        super(MonitoringState.WAIT_FOR_SE_REMOVAL, reader);
    }

    public WaitForSeRemoval(AbstractObservableLocalReader reader, MonitoringJob monitoringJob,
            ExecutorService executorService) {
        super(MonitoringState.WAIT_FOR_SE_REMOVAL, reader, monitoringJob, executorService);
    }

    @Override
    public void onEvent(AbstractObservableLocalReader.InternalEvent event) {
        logger.trace("[{}] onEvent => Event {} received in currentState {}", reader.getName(),
                event, state);

        /*
         * Process InternalEvent
         */
        switch (event) {
            case SE_REMOVED:
                // the SE has been removed, we close all channels and return to
                // the currentState of waiting
                // for insertion
                // We notify the application of the SE_REMOVED event.
                reader.processSeRemoved();
                if (reader.getPollingMode() == ObservableReader.PollingMode.REPEATING) {
                    switchState(MonitoringState.WAIT_FOR_SE_INSERTION);
                } else {
                    switchState(MonitoringState.WAIT_FOR_START_DETECTION);
                }
                break;

            case STOP_DETECT:
                reader.processSeRemoved();
                switchState(MonitoringState.WAIT_FOR_START_DETECTION);
                break;

            default:
                logger.warn("[{}] Ignore =>  Event {} received in currentState {}",
                        reader.getName(), event, state);
                break;
        }
    }

}
