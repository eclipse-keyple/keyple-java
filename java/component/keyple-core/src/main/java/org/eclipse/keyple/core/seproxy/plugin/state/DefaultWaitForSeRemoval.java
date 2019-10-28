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

import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableLocalReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultWaitForSeRemoval extends AbstractObservableState {

    /** logger */
    private static final Logger logger = LoggerFactory.getLogger(DefaultWaitForSeRemoval.class);

    public DefaultWaitForSeRemoval(AbstractObservableLocalReader reader) {
        super(MonitoringState.WAIT_FOR_SE_REMOVAL, reader);
    }

    @Override
    public void onEvent(AbstractObservableLocalReader.InternalEvent event) {
        logger.trace("[{}] onEvent => Event {} received in currentState {}", reader.getName(),
                event, state);
        switch (event) {
            case SE_REMOVED:
                // the SE has been removed, we close all channels and return to
                // the currentState of waiting
                // for insertion
                // We notify the application of the SE_REMOVED event.
                reader.processSeRemoved();
                if (reader.getPollingMode() == ObservableReader.PollingMode.CONTINUE) {
                    switchState(MonitoringState.WAIT_FOR_SE_INSERTION);
                } else {
                    switchState(MonitoringState.WAIT_FOR_START_DETECTION);
                }
                break;

            case TIME_OUT:
                switchState(MonitoringState.WAIT_FOR_START_DETECTION);
                // We notify the application of the TIMEOUT_ERROR event.
                reader.notifyObservers(new ReaderEvent(this.reader.getPluginName(),
                        this.reader.getName(), ReaderEvent.EventType.TIMEOUT_ERROR, null));
                logger.warn("The time limit for the removal of the SE has been exceeded.");
                break;

            default:
                logger.trace("Ignore event");
        }
    }

    @Override
    public void activate() {}

    @Override
    public void deActivate() {}

}
