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
package org.eclipse.keyple.core.reader.plugin.reader;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.keyple.core.reader.Reader;
import org.eclipse.keyple.core.reader.exception.KeypleReaderIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the internal state of an AbstractObservableLocalReader Process InternalEvent against the
 * current state
 *
 * @since 0.9
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
  private final ExecutorService executorService;

  private ObservableReaderStateService(
      AbstractObservableLocalReader reader,
      Map<AbstractObservableState.MonitoringState, AbstractObservableState> states,
      AbstractObservableState.MonitoringState initState,
      ExecutorService executorService) {
    this.states = states;
    this.reader = reader;
    this.executorService = executorService;
    switchState(initState);
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

  /**
   * Builder providing steps to configure ObservableReaderStateService
   *
   * @param reader: Not null instance of {@link AbstractObservableLocalReader}
   * @return A non null instance
   * @since 1.0
   */
  public static SeInsertionStep builder(AbstractObservableLocalReader reader) {
    return new Builder(reader);
  }

  /**
   * Provide step to setup the type of SeInsertion State detection
   *
   * @since 1.0
   */
  public interface SeInsertionStep {
    /**
     * Set up SeInsertionDetection with Native reader ability
     *
     * @return A non null reference
     * @since 1.0
     */
    SeProcessingStep WaitForCardInsertionWithNativeDetection();

    /**
     * Set up SeInsertionDetection to detect the card insertion thanks to the method {@link
     * SmartInsertionReader#waitForCardPresent()}.
     *
     * @return A non null reference
     * @throws KeypleReaderIOException if reader does not implements SmartInsertionReader
     * @since 1.0
     */
    SeProcessingStep WaitForCardInsertionWithSmartDetection();

    /**
     * Set up SeInsertionDetection to use polling of the {@link Reader#isCardPresent()} method to
     * detect CARD_INSERTED
     *
     * @return A non null reference
     * @since 1.0
     */
    SeProcessingStep WaitForCardInsertionWithPollingDetection();
  }

  /**
   * Provide step to setup the type of SeProcessing State detection
   *
   * @since 1.0
   */
  public interface SeProcessingStep {
    /**
     * Set up SeProcessingDetection with Native reader ability
     *
     * @return A non null reference
     * @since 1.0
     */
    SeRemovalStep WaitForCardProcessingWithNativeDetection();

    /**
     * Set up SeProcessingDetection to detect processing thanks to the method {@link
     * SmartRemovalReader#waitForCardAbsentNative()}.
     *
     * @return A non null reference
     * @throws KeypleReaderIOException if reader does not implements SmartRemovalReader
     * @since 1.0
     */
    SeRemovalStep WaitForCardProcessingWithSmartDetection();
  }

  /**
   * Provide step to setup the type of SeRemovale State detection
   *
   * @since 1.0
   */
  public interface SeRemovalStep {
    /**
     * Set up SeRemovalDetection with Native reader ability
     *
     * @return A non null reference
     * @since 1.0
     */
    BuilderStep WaitForCardRemovalWithNativeDetection();

    /**
     * Set up SeRemovalDetection to detect processing thanks to the method {@link
     * SmartRemovalReader#waitForCardAbsentNative()}.
     *
     * @return A non null reference
     * @throws KeypleReaderIOException if reader does not implements SmartRemovalReader
     * @since 1.0
     */
    BuilderStep WaitForCardRemovalWithSmartDetection();

    /**
     * Set up SeRemovalDetection with to use polling of the {@link Reader#isCardPresent()} method to
     * detect CARD_REMOVED
     *
     * @return A non null reference
     * @since 1.0
     */
    BuilderStep WaitForCardRemovalWithPollingDetection();
  }

  /**
   * Provide the last step to build ObservableReaderStateService
   *
   * @since 1.0
   */
  public interface BuilderStep {
    /**
     * Build instance of ObservableReaderStateService
     *
     * @return A non null instance
     * @since 1.0
     */
    ObservableReaderStateService build();
  }

  private static class Builder
      implements SeInsertionStep, SeProcessingStep, SeRemovalStep, BuilderStep {

    private Map<AbstractObservableState.MonitoringState, AbstractObservableState> states =
        new HashMap<AbstractObservableState.MonitoringState, AbstractObservableState>();

    private AbstractObservableLocalReader reader;
    private final ExecutorService executorService;

    private Builder(AbstractObservableLocalReader reader) {
      this.reader = reader;
      this.executorService = Executors.newSingleThreadExecutor();
      this.states.put(
          AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION,
          new WaitForStartDetectState(this.reader));
    }

    /** @see SeInsertionStep#WaitForCardInsertionWithNativeDetection() */
    @Override
    public SeProcessingStep WaitForCardInsertionWithNativeDetection() {
      states.put(
          AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION,
          new WaitForCardInsertionState(this.reader));
      return this;
    }

    /** @see SeInsertionStep#WaitForCardInsertionWithPollingDetection() */
    @Override
    public SeProcessingStep WaitForCardInsertionWithPollingDetection() {
      CardPresentMonitoringJob cardPresentMonitoringJob =
          new CardPresentMonitoringJob(reader, 200, true);
      states.put(
          AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION,
          new WaitForCardInsertionState(
              this.reader, cardPresentMonitoringJob, this.executorService));
      return this;
    }

    /** @see SeInsertionStep#WaitForCardInsertionWithSmartDetection() */
    @Override
    public SeProcessingStep WaitForCardInsertionWithSmartDetection() {
      if (!(reader instanceof SmartInsertionReader))
        throw new KeypleReaderIOException("Reader should implement SmartInsertionReader");
      final SmartInsertionMonitoringJob smartInsertionMonitoringJob =
          new SmartInsertionMonitoringJob((SmartInsertionReader) reader);
      states.put(
          AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION,
          new WaitForCardInsertionState(
              this.reader, smartInsertionMonitoringJob, this.executorService));
      return this;
    }

    /** @see SeProcessingStep#WaitForCardProcessingWithNativeDetection() */
    @Override
    public SeRemovalStep WaitForCardProcessingWithNativeDetection() {
      this.states.put(
          AbstractObservableState.MonitoringState.WAIT_FOR_SE_PROCESSING,
          new WaitForCardProcessingState(this.reader));
      return this;
    }

    /** @see SeProcessingStep#WaitForCardProcessingWithSmartDetection() */
    @Override
    public SeRemovalStep WaitForCardProcessingWithSmartDetection() {
      if (!(reader instanceof SmartRemovalReader))
        throw new KeypleReaderIOException("Reader should implement SmartRemovalReader");
      final SmartRemovalMonitoringJob smartRemovalMonitoringJob =
          new SmartRemovalMonitoringJob((SmartRemovalReader) reader);
      this.states.put(
          AbstractObservableState.MonitoringState.WAIT_FOR_SE_PROCESSING,
          new WaitForCardProcessingState(
              this.reader, smartRemovalMonitoringJob, this.executorService));
      return this;
    }

    /** @see SeRemovalStep#WaitForCardRemovalWithNativeDetection() */
    @Override
    public BuilderStep WaitForCardRemovalWithNativeDetection() {
      states.put(
          AbstractObservableState.MonitoringState.WAIT_FOR_SE_REMOVAL,
          new WaitForCardRemovalState(this.reader));
      return this;
    }

    /** @see SeRemovalStep#WaitForCardRemovalWithPollingDetection() */
    @Override
    public BuilderStep WaitForCardRemovalWithPollingDetection() {
      CardAbsentPingMonitoringJob cardAbsentPingMonitoringJob =
          new CardAbsentPingMonitoringJob(this.reader);
      states.put(
          AbstractObservableState.MonitoringState.WAIT_FOR_SE_REMOVAL,
          new WaitForCardRemovalState(
              this.reader, cardAbsentPingMonitoringJob, this.executorService));
      return this;
    }

    /** @see SeRemovalStep#WaitForCardRemovalWithSmartDetection() */
    @Override
    public BuilderStep WaitForCardRemovalWithSmartDetection() {
      if (!(reader instanceof SmartRemovalReader))
        throw new KeypleReaderIOException("Reader should implement SmartRemovalReader");
      final SmartRemovalMonitoringJob smartRemovalMonitoringJob =
          new SmartRemovalMonitoringJob((SmartRemovalReader) reader);
      states.put(
          AbstractObservableState.MonitoringState.WAIT_FOR_SE_REMOVAL,
          new WaitForCardRemovalState(
              this.reader, smartRemovalMonitoringJob, this.executorService));
      return this;
    }

    /** @see BuilderStep#build() */
    @Override
    public ObservableReaderStateService build() {
      return new ObservableReaderStateService(
          reader,
          states,
          AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION,
          executorService);
    }
  }
}
