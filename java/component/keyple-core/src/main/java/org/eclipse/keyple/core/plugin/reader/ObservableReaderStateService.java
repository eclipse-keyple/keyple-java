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
package org.eclipse.keyple.core.plugin.reader;

import java.util.EnumMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (package-private)<br>
 * Manages the internal state of an AbstractObservableLocalReader Process InternalEvent against the
 * current state
 *
 * @since 0.9
 */
class ObservableReaderStateService {

  /** logger */
  private static final Logger logger = LoggerFactory.getLogger(ObservableReaderStateService.class);

  /** AbstractObservableLocalReader to manage event and states */
  private final AbstractObservableLocalReader reader;

  /** Executor service to provide a unique thread used by the various monitoring jobs */
  private final ExecutorService executorService;

  /** Map of all instantiated states possible */
  private final EnumMap<AbstractObservableState.MonitoringState, AbstractObservableState> states;

  /** Current currentState of the Observable Reader */
  private AbstractObservableState currentState;

  /**
   * (package-private)<br>
   * Initializes the states according to the interfaces implemented by the provided reader.
   *
   * @param reader The current reader
   */
  ObservableReaderStateService(AbstractObservableLocalReader reader) {
    this.reader = reader;
    this.states =
        new EnumMap<AbstractObservableState.MonitoringState, AbstractObservableState>(
            AbstractObservableState.MonitoringState.class);
    this.executorService = Executors.newSingleThreadExecutor();

    // initialize states for each cases:

    // wait for start
    this.states.put(
        AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION,
        new WaitForStartDetectState(this.reader));

    // insertion
    if (reader instanceof WaitForCardInsertionAutonomous) {
      this.states.put(
          AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION,
          new WaitForCardInsertionState(this.reader));
    } else if (reader instanceof WaitForCardInsertionNonBlocking) {
      CardPresentMonitoringJob cardPresentMonitoringJob =
          new CardPresentMonitoringJob(reader, 200, true);
      this.states.put(
          AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION,
          new WaitForCardInsertionState(
              this.reader, cardPresentMonitoringJob, this.executorService));
    } else if (reader instanceof WaitForCardInsertionBlocking) {
      final SmartInsertionMonitoringJob smartInsertionMonitoringJob =
          new SmartInsertionMonitoringJob((WaitForCardInsertionBlocking) reader);
      states.put(
          AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION,
          new WaitForCardInsertionState(
              this.reader, smartInsertionMonitoringJob, this.executorService));
    } else {
      throw new KeypleReaderIOException(
          "Reader should implement implement a WaitForCardInsertion interface.");
    }

    // processing
    if (reader instanceof WaitForCardRemovalDuringProcessing) {
      final SmartRemovalMonitoringJob smartRemovalMonitoringJob =
          new SmartRemovalMonitoringJob((WaitForCardRemovalBlocking) reader);
      this.states.put(
          AbstractObservableState.MonitoringState.WAIT_FOR_SE_PROCESSING,
          new WaitForCardProcessingState(
              this.reader, smartRemovalMonitoringJob, this.executorService));
    } else if (reader instanceof DontWaitForCardRemovalDuringProcessing) {
      this.states.put(
          AbstractObservableState.MonitoringState.WAIT_FOR_SE_PROCESSING,
          new WaitForCardProcessingState(this.reader));
    } else {
      throw new KeypleReaderIOException(
          "Reader should implement implement a Wait/DontWait ForCardRemovalDuringProcessing interface.");
    }

    // removal
    if (reader instanceof WaitForCardRemovalAutonomous) {
      this.states.put(
          AbstractObservableState.MonitoringState.WAIT_FOR_SE_REMOVAL,
          new WaitForCardRemovalState(this.reader));

    } else if (reader instanceof WaitForCardRemovalNonBlocking) {
      CardAbsentPingMonitoringJob cardAbsentPingMonitoringJob =
          new CardAbsentPingMonitoringJob(this.reader);
      this.states.put(
          AbstractObservableState.MonitoringState.WAIT_FOR_SE_REMOVAL,
          new WaitForCardRemovalState(
              this.reader, cardAbsentPingMonitoringJob, this.executorService));
    } else if (reader instanceof WaitForCardRemovalBlocking) {
      final SmartRemovalMonitoringJob smartRemovalMonitoringJob =
          new SmartRemovalMonitoringJob((WaitForCardRemovalBlocking) reader);
      states.put(
          AbstractObservableState.MonitoringState.WAIT_FOR_SE_REMOVAL,
          new WaitForCardRemovalState(
              this.reader, smartRemovalMonitoringJob, this.executorService));
    } else {
      throw new KeypleReaderIOException(
          "Reader should implement implement a WaitForCardRemoval interface.");
    }

    switchState(AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION);
  }

  /**
   * Thread safe method to communicate an internal event to this reader Use this method to inform
   * the reader of external event like a tag discovered or a card inserted
   *
   * @param event internal event
   * @since 0.9
   */
  protected final synchronized void onEvent(AbstractObservableLocalReader.InternalEvent event) {
    this.currentState.onEvent(event);
  }

  /**
   * Thread safe method to switch the state of this reader should only be invoked by this reader or
   * its state
   *
   * @param stateId : next state to onActivate
   * @since 0.9
   */
  public final synchronized void switchState(AbstractObservableState.MonitoringState stateId) {

    if (currentState != null) {
      if (logger.isTraceEnabled()) {
        logger.trace(
            "[{}] Switch currentState from {} to {}",
            this.reader.getName(),
            this.currentState.getMonitoringState(),
            stateId);
      }
      currentState.onDeactivate();
    } else {
      if (logger.isTraceEnabled()) {
        logger.trace("[{}] Switch to a new currentState {}", this.reader.getName(), stateId);
      }
    }

    // switch currentState
    currentState = this.states.get(stateId);

    if (logger.isTraceEnabled()) {
      logger.trace(
          "[{}] New currentState {}", this.reader.getName(), currentState.getMonitoringState());
    }
    // onActivate the new current state
    currentState.onActivate();
  }

  /**
   * Get reader current state
   *
   * @return reader current state
   * @since 0.9
   */
  protected final synchronized AbstractObservableState getCurrentState() {
    return currentState;
  }

  /**
   * Get the reader current monitoring state
   *
   * @return current monitoring state
   * @since 0.9
   */
  public final synchronized AbstractObservableState.MonitoringState getCurrentMonitoringState() {
    return this.currentState.getMonitoringState();
  }
}
