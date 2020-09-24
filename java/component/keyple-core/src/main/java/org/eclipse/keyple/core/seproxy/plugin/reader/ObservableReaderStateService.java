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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the internal state of an AbstractObservableLocalReader Process InternalEvent against the
 * current state
 */
public class ObservableReaderStateService {

  /** logger */
  private static final Logger logger = LoggerFactory.getLogger(ObservableReaderStateService.class);

  /* AbstractObservableLocalReader to manage event and states */
  private final AbstractObservableLocalReader reader;

  /* Map of all instantiated states possible */
  private final Map<AbstractObservableState.MonitoringState, AbstractObservableState> states;

  /* Current currentState of the Observable Reader */
  private AbstractObservableState currentState;

  /* Single Executor to run State Service watch */
  private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

  public ObservableReaderStateService(
      AbstractObservableLocalReader reader,
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
  public final synchronized void onEvent(AbstractObservableLocalReader.InternalEvent event) {
    this.currentState.onEvent(event);
  }

  /**
   * Thread safe method to switch the state of this reader should only be invoked by this reader or
   * its state
   *
   * @param stateId : next state to onActivate
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
   */
  protected final synchronized AbstractObservableState getCurrentState() {
    return currentState;
  }

  /**
   * Get the reader current monitoring state
   *
   * @return current monitoring state
   */
  public final synchronized AbstractObservableState.MonitoringState getCurrentMonitoringState() {
    return this.currentState.getMonitoringState();
  }

  public interface StartWithStep {
    public WaitForStep startWithWaitForStart();

    public WaitForStep startWithWaitForSeInsertion();
  }

  public interface WaitForStep {
    public DetectionStep waitForSeInsertion();

    public DetectionStep waitForSeRemoval();

    public ObservableReaderStateService build();
  }

  public interface DetectionStep {
    public WaitForStep withNativeDetection();

    public WaitForStep withSmartDetection();

    public WaitForStep withPollingDetection();
  }

  public static class Builder implements StartWithStep, WaitForStep, DetectionStep {

    private Map<AbstractObservableState.MonitoringState, AbstractObservableState> states =
        new HashMap<AbstractObservableState.MonitoringState, AbstractObservableState>();

    private ObservableReader reader;
    private AbstractObservableState.MonitoringState startState;
    private AbstractObservableState.MonitoringState bufferedState;

    public Builder(ObservableReader reader) {
      this.reader = reader;
      this.states.put(
          AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION,
          new WaitForStartDetectState((AbstractObservableLocalReader) this.reader));
      this.states.put(
          AbstractObservableState.MonitoringState.WAIT_FOR_SE_PROCESSING,
          new WaitForSeProcessingState((AbstractObservableLocalReader) this.reader));
    }

    @Override
    public WaitForStep startWithWaitForStart() {
      startState = AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION;
      return this;
    }

    @Override
    public WaitForStep startWithWaitForSeInsertion() {
      startState = AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION;
      return this;
    }

    @Override
    public DetectionStep waitForSeInsertion() {
      bufferedState = AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION;
      return this;
    }

    @Override
    public DetectionStep waitForSeRemoval() {
      bufferedState = AbstractObservableState.MonitoringState.WAIT_FOR_SE_REMOVAL;
      return this;
    }

    @Override
    public WaitForStep withNativeDetection() {
      if (bufferedState == null)
        throw new KeypleReaderIOException(
            "waitForSeInsertion() or waitForSeRemoval() must be call before");
      switch (bufferedState) {
        case WAIT_FOR_SE_INSERTION:
          states.put(
              AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION,
              new WaitForSeInsertionState((AbstractObservableLocalReader) this.reader));
          break;
        case WAIT_FOR_SE_REMOVAL:
          states.put(
              AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION,
              new WaitForSeRemovalState((AbstractObservableLocalReader) this.reader));
          break;
      }
      bufferedState = null;
      return this;
    }

    @Override
    public WaitForStep withSmartDetection() {
      if (bufferedState == null)
        throw new KeypleReaderIOException(
            "waitForSeInsertion() or waitForSeRemoval() must be call before");
      switch (bufferedState) {
        case WAIT_FOR_SE_INSERTION:
          if (!(reader instanceof SmartInsertionReader))
            throw new KeypleReaderIOException("Reader should implement SmartRemovalReader");
          SmartInsertionMonitoringJob smartInsertionMonitoringJob =
              new SmartInsertionMonitoringJob((SmartInsertionReader) reader);
          states.put(
              AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION,
              new WaitForStartDetectState(
                  (AbstractObservableLocalReader) this.reader,
                  smartInsertionMonitoringJob,
                  executorService));
          break;
        case WAIT_FOR_SE_REMOVAL:
          if (!(reader instanceof SmartRemovalReader))
            throw new KeypleReaderIOException("Reader should implement SmartRemovalReader");
          SmartRemovalMonitoringJob smartRemovalMonitoringJob =
              new SmartRemovalMonitoringJob((SmartRemovalReader) reader);
          states.put(
              AbstractObservableState.MonitoringState.WAIT_FOR_SE_REMOVAL,
              new WaitForSeRemovalState(
                  (AbstractObservableLocalReader) this.reader,
                  smartRemovalMonitoringJob,
                  executorService));
          break;
      }
      bufferedState = null;
      return this;
    }

    @Override
    public WaitForStep withPollingDetection() {
      if (bufferedState == null)
        throw new KeypleReaderIOException(
            "waitForSeInsertion() or waitForSeRemoval() must be call before");
      switch (bufferedState) {
        case WAIT_FOR_SE_INSERTION:
          CardPresentMonitoringJob cardPresentMonitoringJob =
              new CardPresentMonitoringJob(reader, 1000, true);
          states.put(
              AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION,
              new WaitForStartDetectState(
                  (AbstractObservableLocalReader) this.reader,
                  cardPresentMonitoringJob,
                  executorService));
          break;
        case WAIT_FOR_SE_REMOVAL:
          CardAbsentPingMonitoringJob cardAbsentPingMonitoringJob =
              new CardAbsentPingMonitoringJob((AbstractObservableLocalReader) this.reader);
          states.put(
              AbstractObservableState.MonitoringState.WAIT_FOR_SE_REMOVAL,
              new WaitForSeRemovalState(
                  (AbstractObservableLocalReader) this.reader,
                  cardAbsentPingMonitoringJob,
                  executorService));
          break;
      }
      bufferedState = null;
      return this;
    }

    @Override
    public ObservableReaderStateService build() {
      if (startState == null) {
        startState = AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION;
      }
      return new ObservableReaderStateService(
          (AbstractObservableLocalReader) reader, states, startState);
    }
  }
}
