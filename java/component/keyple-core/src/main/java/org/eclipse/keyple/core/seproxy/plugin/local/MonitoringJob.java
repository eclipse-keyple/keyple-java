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
package org.eclipse.keyple.core.seproxy.plugin.local;


/**
 * Monitoring jobs interface
 */
public interface MonitoringJob {

    /**
     * Define a Runnable task of the monitoring job
     * 
     * @param state referentce to the state he monitoring job in running againts
     * @return routine that will be executed in background of the state
     */
    Runnable getMonitoringJob(AbstractObservableState state);
}
