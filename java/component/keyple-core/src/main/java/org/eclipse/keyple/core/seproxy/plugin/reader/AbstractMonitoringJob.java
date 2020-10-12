/* **************************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.seproxy.plugin.reader;

/**
 * Monitoring jobs abstract class.
 *
 * <p>Observable readers can instantiate {@link AbstractMonitoringJob} to perform background
 * processing during the different states of the generic state machine.
 *
 * <p>Internal events ({@link AbstractObservableLocalReader.InternalEvent}) can be fired to change
 * the state of the machine via the {@link AbstractObservableState} class passed as a constructor's
 * argument.
 *
 * <p>Standard {@link AbstractMonitoringJob} are already defined in the local.monitoring package but
 * it is possible to define new ones within a plugin reader if necessary, implementing this abstract
 * class.
 */
abstract class AbstractMonitoringJob {

  /**
   * Define a Runnable task of the monitoring job
   *
   * @param state reference to the state the monitoring job in running against
   * @return routine that will be executed in background of the state
   */
  abstract Runnable getMonitoringJob(AbstractObservableState state);

  /** Should stop/interrupt the monitoring job */
  abstract void stop();
}
