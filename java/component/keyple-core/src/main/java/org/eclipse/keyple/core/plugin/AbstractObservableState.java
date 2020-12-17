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
package org.eclipse.keyple.core.plugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a state behaviour for a {@link AbstractObservableLocalReader}.
 *
 * <p>Handles AbstractObservableLocalReader.InternalEvent that might results on a switch of state.
 *
 * @since 0.9
 */
abstract class AbstractObservableState {

  private static final Logger logger = LoggerFactory.getLogger(AbstractObservableState.class);

  /** The states that the reader monitoring currentState machine can have */
  public enum MonitoringState {
    WAIT_FOR_START_DETECTION,
    WAIT_FOR_SE_INSERTION,
    WAIT_FOR_SE_PROCESSING,
    WAIT_FOR_SE_REMOVAL
  }

  /* Identifier of the currentState */
  protected MonitoringState state;

  /* Reference to Reader */
  protected AbstractObservableLocalReader reader;

  /* Background job definition if any */
  protected AbstractMonitoringJob monitoringJob;

  /* Result of the background job if any */
  protected Future<?> monitoringEvent;

  /* Executor service used to execute AbstractMonitoringJob */
  protected ExecutorService executorService;

  /**
   * Create a new state with a state identifier and a monitor job
   *
   * @param state the state identifier
   * @param reader the current reader
   * @param monitoringJob the job to be executed in background (may be null if no background job is
   *     required)
   * @param executorService the executor service
   * @since 0.9
   */
  protected AbstractObservableState(
      MonitoringState state,
      AbstractObservableLocalReader reader,
      AbstractMonitoringJob monitoringJob,
      ExecutorService executorService) {
    this.reader = reader;
    this.state = state;
    this.monitoringJob = monitoringJob;
    this.executorService = executorService;
  }

  /**
   * Create a new state with a state identifier
   *
   * @param reader observable reader this currentState is attached to
   * @param state name of the currentState
   * @since 0.9
   */
  protected AbstractObservableState(MonitoringState state, AbstractObservableLocalReader reader) {
    this.reader = reader;
    this.state = state;
  }

  /**
   * Get the current state identifier of the state machine
   *
   * @return the current state identifier
   * @since 0.9
   */
  public MonitoringState getMonitoringState() {
    return state;
  }

  /**
   * Switch state in the parent reader
   *
   * @param stateId the new state
   * @since 0.9
   */
  protected void switchState(AbstractObservableState.MonitoringState stateId) {
    reader.switchState(stateId);
  }

  /**
   * Handle Internal Event Usually state is switched using method reader::switchState
   *
   * @param event internal event received by reader
   * @since 0.9
   */
  abstract void onEvent(AbstractObservableLocalReader.InternalEvent event);

  /**
   * Invoked when activated, a custom behaviour can be added here
   *
   * @since 0.9
   */
  void onActivate() {
    if (logger.isTraceEnabled()) {
      logger.trace("[{}] onActivate => {}", this.reader.getName(), this.getMonitoringState());
    }
    // launch the monitoringJob is necessary
    if (monitoringJob != null) {
      if (executorService == null) {
        throw new AssertionError("ExecutorService must be set");
      }
      monitoringEvent = executorService.submit(monitoringJob.getMonitoringJob(this));
    }
  }

  /**
   * Invoked when deactivated
   *
   * @since 0.9
   */
  void onDeactivate() {
    if (logger.isTraceEnabled()) {
      logger.trace("[{}] onDeactivate => {}", this.reader.getName(), this.getMonitoringState());
    }
    // cancel the monitoringJob is necessary
    if (monitoringEvent != null && !monitoringEvent.isDone()) {
      monitoringJob.stop();

      // TODO this could be inside the stop method?
      boolean canceled = monitoringEvent.cancel(false);
      if (logger.isTraceEnabled()) {
        logger.trace(
            "[{}] onDeactivate => cancel runnable waitForCarPresent by thead interruption {}",
            reader.getName(),
            canceled);
      }
    }
  }
}
