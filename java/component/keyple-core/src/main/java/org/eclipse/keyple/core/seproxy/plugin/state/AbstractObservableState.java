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

import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableLocalReader;

public abstract class AbstractObservableState {

    /** The states that the reader monitoring currentState machine can have */
    public enum MonitoringState {
        WAIT_FOR_START_DETECTION, WAIT_FOR_SE_INSERTION, WAIT_FOR_SE_PROCESSING, WAIT_FOR_SE_REMOVAL
    }

    /* Identifier of the currentState */
    protected MonitoringState state;

    /* Reference to Reader */
    protected AbstractObservableLocalReader reader;

    /**
     * Create a new currentState with a currentState identifier
     * 
     * @param reader : observable reader this currentState is attached to
     * @param state : name of the currentState
     */
    protected AbstractObservableState(MonitoringState state, AbstractObservableLocalReader reader) {
        this.reader = reader;
        this.state = state;
    }

    /**
     * Get currentState identifier
     * 
     * @return name currentState
     */
    public MonitoringState getMonitoringState() {
        return state;
    }

    /**
     * Switch state in the parent reader
     * 
     * @param stateId the new state
     */
    protected void switchState(AbstractObservableState.MonitoringState stateId) {
        reader.switchState(stateId);
    }

    /**
     * Handle Internal Event Usually state is switched using method reader::switchState
     * 
     * @param event internal event received by reader
     */
    public abstract void onEvent(AbstractObservableLocalReader.InternalEvent event);

    /**
     * If needed, state can be activated (for instance for polling mecanism)
     */
    public abstract void activate();

    /**
     * If needed, deactivate state
     */
    public abstract void deActivate();



}
