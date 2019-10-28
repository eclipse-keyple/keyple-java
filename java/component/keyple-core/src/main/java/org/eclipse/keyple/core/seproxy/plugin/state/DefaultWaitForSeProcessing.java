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
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableLocalReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultWaitForSeProcessing extends AbstractObservableState {

    /** logger */
    private static final Logger logger = LoggerFactory.getLogger(DefaultWaitForSeProcessing.class);

    public DefaultWaitForSeProcessing(AbstractObservableLocalReader reader) {
        super(MonitoringState.WAIT_FOR_SE_PROCESSING, reader);
    }

    @Override
    public void onEvent(AbstractObservableLocalReader.InternalEvent event) {
        logger.trace("Event {} received on reader {} in currentState {}", event, reader.getName(),
                state);
        switch (event) {
            case SE_PROCESSED:
                if (this.reader.getPollingMode() == ObservableReader.PollingMode.CONTINUE) {
                    this.reader.switchState(MonitoringState.WAIT_FOR_SE_REMOVAL);
                } else {
                    // We close the channels now and notify the application of
                    // the SE_REMOVED event.
                    this.reader.processSeRemoved();
                    this.reader.switchState(MonitoringState.WAIT_FOR_START_DETECTION);
                }
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
