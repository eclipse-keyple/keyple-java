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
package org.eclipse.keyple.core.seproxy.plugin;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the internal state of an AbstractObservableLocalReader Process InternalEvent against the
 * current state
 */
public class ObservableReaderStateService {

    /** logger */
    private static final Logger logger =
            LoggerFactory.getLogger(ObservableReaderStateService.class);

    /* AbstractObservableLocalReader to manage event and states */
    private final AbstractObservableLocalReader reader;

    /* Map of all instantiated states possible */
    private final Map<AbstractObservableState.MonitoringState, AbstractObservableState> states;

    /* Current currentState of the Observable Reader */
    private AbstractObservableState currentState;


    public ObservableReaderStateService(AbstractObservableLocalReader reader,
            Map<AbstractObservableState.MonitoringState, AbstractObservableState> states,
            AbstractObservableState.MonitoringState initState) {
        this.states = states;
        this.reader = reader;
        switchState(initState);
    }

    /**
     * Thread safe method to communicate an internal event to this reader Use this method to inform
     * the reader of external event like a tag discovered or a Se inserted
     *
     * @param event internal event
     */
    final synchronized public void onEvent(AbstractObservableLocalReader.InternalEvent event) {
        this.currentState.onEvent(event);
    }


    /**
     * Thread safe method to switch the state of this reader should only be invoked by this reader
     * or its state
     *
     * @param stateId : next state to onActivate
     */
    final synchronized public void switchState(AbstractObservableState.MonitoringState stateId) {

        if (currentState != null) {
            logger.trace("[{}] Switch currentState from {} to {}", this.reader.getName(),
                    this.currentState.getMonitoringState(), stateId);

            currentState.onDeactivate();
        } else {
            logger.trace("[{}] Switch to a new currentState {}", this.reader.getName(), stateId);
        }

        // switch currentState
        currentState = this.states.get(stateId);

        logger.debug("[{}] New currentState {}", this.reader.getName(),
                currentState.getMonitoringState());

        // onActivate the new current state
        currentState.onActivate();
    }

    /**
     * Get reader current state
     *
     * @return reader current state
     */
    final synchronized protected AbstractObservableState getCurrentState() {
        return currentState;
    }

    /**
     * Get the reader current monitoring state
     *
     * @return current monitoring state
     */
    final synchronized public AbstractObservableState.MonitoringState getCurrentMonitoringState() {
        return this.currentState.getMonitoringState();
    }
}
