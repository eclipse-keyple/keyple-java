/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.CardResponse;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class is used to manage the matter of observing card events in the case of a local
 * reader.
 *
 * <p>It provides the means to configure the plugin's behavior when a card is detected.
 *
 * <p>The event management implements a ObservableReaderStateService state machine that is composed
 * of four states.
 *
 * <ol>
 *   <li>WAIT_FOR_START_DETECTION
 *       <p>Infinitely waiting for a signal from the application to start card detection by changing
 *       to WAIT_FOR_SE_INSERTION state. This signal is given by calling the
 *       setDefaultSelectionRequest method.
 *       <p>Note: The system always starts in the WAIT_FOR_START_DETECTION state.
 *   <li>WAIT_FOR_SE_INSERTION
 *       <p>Awaiting the card insertion. After insertion, the processCardInserted method is called.
 *       <p>A number of cases arise:
 *       <ul>
 *         <li>A default selection is defined: in this case it is played and its result leads to an
 *             event notification CARD_INSERTED or CARD_MATCHED or no event (see
 *             setDefaultSelectionRequest)
 *         <li>There is no default selection: a CARD_INSERTED event is then notified.
 *             <p>In the case where an event has been notified to the application, the state machine
 *             changes to the WAIT_FOR_SE_PROCESSING state otherwise it remains in the
 *             WAIT_FOR_SE_INSERTION state.
 *       </ul>
 *       <p>The notification consists in calling the "update" methods of the defined observers. In
 *       the case where several observers have been defined, it is up to the application developer
 *       to ensure that there is no long processing in these methods, by making their execution
 *       asynchronous for example.
 *   <li>WAIT_FOR_SE_PROCESSING
 *       <p>Waiting for the end of processing by the application. The end signal is triggered by a
 *       transmission made with a CLOSE_AFTER parameter.
 *       <p>If the instruction given when defining the default selection request is to stop
 *       (ObservableReader.PollingMode.SINGLESHOT) then the logical and physical channels are closed
 *       immediately and the machine state changes to WAIT_FOR_START_DETECTION state.
 *       <p>If the instruction given is continue (ObservableReader.PollingMode.REPEATING) then the
 *       state machine changes to WAIT_FOR_SE_REMOVAL.
 *   <li>WAIT_FOR_SE_REMOVAL:
 *       <p>Waiting for the card to be removed. When the card is removed, a CARD_REMOVED event is
 *       notified to the application and the state machine changes to the WAIT_FOR_SE_INSERTION or
 *       WAIT_FOR_START_DETECTION state according the polling mode (ObservableReader.PollingMode).
 * </ol>
 */
public abstract class AbstractObservableLocalReader extends AbstractLocalReader
    implements ObservableReaderNotifier {
  /** logger */
  private static final Logger logger = LoggerFactory.getLogger(AbstractObservableLocalReader.class);

  /** The default DefaultSelectionsRequest to be executed upon card insertion */
  private DefaultSelectionsRequest defaultSelectionsRequest;

  /** Indicate if all cards detected should be notified or only matching cards */
  private ObservableReader.NotificationMode notificationMode;

  private ObservableReader.PollingMode currentPollingMode = ObservableReader.PollingMode.SINGLESHOT;

  /**
   * Internal events
   *
   * @since 0.9
   */
  public enum InternalEvent {
    /** A card has been inserted */
    CARD_INSERTED,
    /** The card has been removed */
    CARD_REMOVED,
    /** The application has completed the processing of the card */
    SE_PROCESSED,
    /** The application has requested the start of card detection */
    START_DETECT,
    /** The application has requested that card detection is to be stopped. */
    STOP_DETECT,
    /** A timeout has occurred (not yet implemented) */
    TIME_OUT
  }

  /* The observers of this object */
  private List<ObservableReader.ReaderObserver> observers;
  /*
   * this object will be used to synchronize the access to the observers list in order to be
   * thread safe
   */
  private final Object sync = new Object();

  /* Service that handles Internal Events and their impact on the current state of the reader */
  protected final ObservableReaderStateService stateService;

  /**
   * Initialize the ObservableReaderStateService with the possible states and their implementation.
   * ObservableReaderStateService define the initial state.
   *
   * <p>Make sure to initialize the stateService in your reader constructor with stateService =
   * initStateService()
   *
   * <p>
   *
   * @return initialized state stateService with possible states and the init state
   */
  protected abstract ObservableReaderStateService initStateService();

  /**
   * Reader constructor
   *
   * <p>Force the definition of a name through the use of super method.
   *
   * <p>
   *
   * @param pluginName the name of the plugin that instantiated the reader
   * @param readerName the name of the reader
   */
  public AbstractObservableLocalReader(String pluginName, String readerName) {
    super(pluginName, readerName);
    stateService = initStateService();
  }

  /**
   * Add a {@link ObservableReader.ReaderObserver}.
   *
   * <p>The observer will receive all the events produced by this reader (card insertion, removal,
   * etc.)
   *
   * @param observer the observer object
   */
  @Override
  public final void addObserver(final ObservableReader.ReaderObserver observer) {
    if (observer == null) {
      return;
    }

    if (logger.isTraceEnabled()) {
      logger.trace(
          "Adding '{}' as an observer of '{}'.", observer.getClass().getSimpleName(), getName());
    }

    synchronized (sync) {
      if (observers == null) {
        observers = new ArrayList<ReaderObserver>(1);
      }
      observers.add(observer);
    }
  }

  /**
   * Remove a {@link ObservableReader.ReaderObserver}.
   *
   * <p>The observer will do not receive any of the events produced by this reader.
   *
   * @param observer the observer object
   */
  @Override
  public final void removeObserver(final ObservableReader.ReaderObserver observer) {
    if (observer == null) {
      return;
    }

    if (logger.isTraceEnabled()) {
      logger.trace("[{}] Deleting a reader observer", getName());
    }

    synchronized (sync) {
      if (observers != null) {
        observers.remove(observer);
      }
    }
  }

  /**
   * Notify all registered observers with the provided {@link ReaderEvent}
   *
   * @param event the reader event
   */
  @Override
  public final void notifyObservers(final ReaderEvent event) {

    if (logger.isTraceEnabled()) {
      logger.trace(
          "[{}] Notifying a reader event to {} observers. EVENTNAME = {}",
          getName(),
          this.countObservers(),
          event.getEventType().name());
    }

    List<ObservableReader.ReaderObserver> observersCopy;

    synchronized (sync) {
      if (observers == null) {
        return;
      }
      observersCopy = new ArrayList<ReaderObserver>(observers);
    }

    for (ObservableReader.ReaderObserver observer : observersCopy) {
      observer.update(event);
    }
  }

  /** @return the number of observers */
  @Override
  public final int countObservers() {
    return observers == null ? 0 : observers.size();
  }

  /** Remove all observers at once */
  @Override
  public final void clearObservers() {
    if (observers != null) {
      this.observers.clear();
    }
  }

  /**
   * Check the presence of a card
   *
   * <p>This method is recommended for non-observable readers.
   *
   * <p>When the card is not present the logical and physical channels status may be refreshed
   * through a call to the processSeRemoved method.
   *
   * @return true if the card is present
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   */
  @Override
  public final boolean isCardPresent() {
    if (checkCardPresence()) {
      return true;
    } else {
      /*
       * if the card is no longer present but one of the channels is still open, then the
       * CARD_REMOVED notification is performed and the channels are closed.
       */
      if (isLogicalChannelOpen() || isPhysicalChannelOpen()) {
        processSeRemoved();
      }
      return false;
    }
  }

  /**
   * Starts the card detection. Once activated, the application can be notified of the arrival of a
   * card.
   *
   * <p>This method must be overloaded by readers depending on the particularity of their management
   * of the start of the card detection.
   *
   * <p>Note: they must call the super method with the argument PollingMode.
   *
   * @param pollingMode indicates the action to be followed after processing the card: if REPEATING,
   *     the card detection is restarted, if SINGLESHOT, the card detection is stopped until a new
   *     call to startCardDetection is made.
   */
  @Override
  public final void startCardDetection(ObservableReader.PollingMode pollingMode) {
    if (logger.isTraceEnabled()) {
      logger.trace("[{}] start the card Detection with pollingMode {}", getName(), pollingMode);
    }
    this.currentPollingMode = pollingMode;
    this.stateService.onEvent(InternalEvent.START_DETECT);
  }

  /**
   * Stops the card detection.
   *
   * <p>This method must be overloaded by readers depending on the particularity of their management
   * of the start of card detection.
   */
  @Override
  public final void stopCardDetection() {
    if (logger.isTraceEnabled()) {
      logger.trace("[{}] stop the card Detection", getName());
    }
    this.stateService.onEvent(InternalEvent.STOP_DETECT);
  }

  /**
   * If defined, the prepared DefaultSelectionRequest will be processed as soon as a card is
   * inserted. The result of this request set will be added to the reader event notified to the
   * application.
   *
   * <p>If it is not defined (set to null), a simple card detection will be notified in the end.
   *
   * <p>Depending on the notification mode, the observer will be notified whenever a card is
   * inserted, regardless of the selection status, or only if the current card matches the selection
   * criteria.
   *
   * <p>
   *
   * @param defaultSelectionsRequest the {@link AbstractDefaultSelectionsRequest} to be executed
   *     when a card is inserted
   * @param notificationMode the notification mode enum (ALWAYS or MATCHED_ONLY)
   */
  @Override
  public final void setDefaultSelectionRequest(
      AbstractDefaultSelectionsRequest defaultSelectionsRequest,
      ObservableReader.NotificationMode notificationMode) {
    this.defaultSelectionsRequest = (DefaultSelectionsRequest) defaultSelectionsRequest;
    this.notificationMode = notificationMode;
  }

  /**
   * A combination of defining the default selection request and starting the card detection.
   *
   * @param defaultSelectionsRequest the selection request to be operated
   * @param notificationMode indicates whether a CARD_INSERTED event should be notified even if the
   *     selection has failed (ALWAYS) or whether the card insertion should be ignored in this case
   *     (MATCHED_ONLY).
   * @param pollingMode indicates the action to be followed after processing the card: if CONTINUE,
   *     the card detection is restarted, if STOP, the card detection is stopped until a new call to
   *     startCardDetection is made.
   */
  @Override
  public final void setDefaultSelectionRequest(
      AbstractDefaultSelectionsRequest defaultSelectionsRequest,
      ObservableReader.NotificationMode notificationMode,
      ObservableReader.PollingMode pollingMode) {
    // define the default selection request
    setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode);
    // initiates the card detection
    startCardDetection(pollingMode);
  }

  /**
   * (package-private)<br>
   * This method initiates the card removal sequence.
   *
   * <p>The reader will remain in the WAIT_FOR_SE_REMOVAL state as long as the card is present. It
   * will change to the WAIT_FOR_START_DETECTION or WAIT_FOR_SE_INSERTION state depending on what
   * was set when the detection was started.
   */
  @Override
  final void terminateSeCommunication() {
    if (logger.isTraceEnabled()) {
      logger.trace("[{}] start removal sequence of the reader", getName());
    }
    this.stateService.onEvent(InternalEvent.SE_PROCESSED);
  }

  /**
   * (package-private)<br>
   * This method is invoked when a card is inserted in the case of an observable reader.
   *
   * <p>e.g. from the monitoring thread in the case of a Pcsc plugin or from the NfcAdapter callback
   * method onTagDiscovered in the case of a Android NFC plugin.
   *
   * <p>It will return a ReaderEvent in the following cases:
   *
   * <ul>
   *   <li>CARD_INSERTED: if no default selection request was defined
   *   <li>CARD_MATCHED: if a default selection request was defined in any mode and a card matched
   *       the selection
   *   <li>CARD_INSERTED: if a default selection request was defined in ALWAYS mode but no card
   *       matched the selection (the DefaultSelectionsResponse is however transmitted)
   * </ul>
   *
   * <p>It returns null if a default selection is defined in MATCHED_ONLY mode but no card matched
   * the selection.
   *
   * @return ReaderEvent that should be notified to observers, contains the results of the default
   *     selection if any, can be null if no event should be sent
   */
  ReaderEvent processCardInserted() {
    if (logger.isTraceEnabled()) {
      logger.trace("[{}] process the inserted card", getName());
    }
    if (defaultSelectionsRequest == null) {
      if (logger.isTraceEnabled()) {
        logger.trace("[{}] no default selection request defined, notify CARD_INSERTED", getName());
      }
      /* no default request is defined, just notify the card insertion */
      return new ReaderEvent(getPluginName(), getName(), ReaderEvent.EventType.CARD_INSERTED, null);
    } else {
      /*
       * a default request is defined, send it and notify according to the notification mode
       * and the selection status
       */
      boolean aSeMatched = false;
      try {
        List<CardResponse> cardResponses =
            transmitCardRequests(
                defaultSelectionsRequest.getSelectionCardRequests(),
                defaultSelectionsRequest.getMultiSelectionProcessing(),
                defaultSelectionsRequest.getChannelControl());

        for (CardResponse cardResponse : cardResponses) {
          if (cardResponse != null && cardResponse.getSelectionStatus().hasMatched()) {
            if (logger.isTraceEnabled()) {
              logger.trace("[{}] a default selection has matched", getName());
            }
            aSeMatched = true;
            break;
          }
        }

        if (notificationMode == ObservableReader.NotificationMode.MATCHED_ONLY) {
          /* notify only if a card matched the selection, just ignore if not */
          if (aSeMatched) {
            return new ReaderEvent(
                getPluginName(),
                getName(),
                ReaderEvent.EventType.CARD_MATCHED,
                new DefaultSelectionsResponse(cardResponses));
          } else {
            if (logger.isTraceEnabled()) {
              logger.trace(
                  "[{}] selection hasn't matched"
                      + " do not thrown any event because of MATCHED_ONLY flag",
                  getName());
            }
            return null;
          }
        } else {
          // ObservableReader.NotificationMode.ALWAYS
          if (aSeMatched) {
            /* the card matched, notify a CARD_MATCHED event with the received response */
            return new ReaderEvent(
                getPluginName(),
                getName(),
                ReaderEvent.EventType.CARD_MATCHED,
                new DefaultSelectionsResponse(cardResponses));
          } else {
            /*
             * the card didn't match, notify an CARD_INSERTED event with the received
             * response
             */
            if (logger.isTraceEnabled()) {
              logger.trace(
                  "[{}] none of {} default selection matched", getName(), cardResponses.size());
            }
            return new ReaderEvent(
                getPluginName(),
                getName(),
                ReaderEvent.EventType.CARD_INSERTED,
                new DefaultSelectionsResponse(cardResponses));
          }
        }
      } catch (KeypleReaderException e) {
        /* the last transmission failed, close the logical and physical channels */
        closeLogicalAndPhysicalChannels();
        if (logger.isDebugEnabled()) {
          logger.debug(
              "An IO Exception occurred while processing the default selection. {}",
              e.getMessage());
        }
        // in this case the card has been removed or not read correctly, do not throw event
      }
    }

    // We close here the physical channel in case it has been opened for a card outside the
    // expected cards
    try {
      closePhysicalChannel();
    } catch (KeypleReaderIOException e) {
      logger.error("Error while closing physical channel. {}", e.getMessage());
    }
    // no event returned
    return null;
  }

  /**
   * (package-private)<br>
   * Sends a neutral APDU to the card to check its presence. The status of the response is not
   * verified as long as the mere fact that the card responds is sufficient to indicate whether or
   * not it is present.
   *
   * <p>This method has to be called regularly until the card no longer respond.
   *
   * <p>Having this method not final allows a reader plugin to implement its own method.
   *
   * @return true if the card still responds, false if not
   */
  boolean isCardPresentPing() {
    // APDU sent to check the communication with the PO
    final byte[] apdu = {(byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    // transmits the APDU and checks for the IO exception.
    try {
      if (logger.isTraceEnabled()) {
        logger.trace("[{}] Ping card", getName());
      }
      transmitApdu(apdu);
    } catch (KeypleReaderIOException e) {
      if (logger.isTraceEnabled()) {
        logger.trace(
            "[{}] Exception occurred in isCardPresentPing. Message: {}", getName(), e.getMessage());
      }
      return false;
    }
    return true;
  }

  /**
   * This method is invoked when a card is removed in the case of an observable reader.
   *
   * <p>It will also be invoked if isCardPresent is called and at least one of the physical or
   * logical channels is still open (case of a non-observable reader)
   *
   * <p>the card will be notified removed only if it has been previously notified present
   * (observable reader only)
   */
  final void processSeRemoved() {
    closeLogicalAndPhysicalChannels();
    notifyObservers(
        new ReaderEvent(getPluginName(), getName(), ReaderEvent.EventType.CARD_REMOVED, null));
  }

  /**
   * (package-private)<br>
   * Get polling mode
   *
   * @return the current polling mode
   */
  ObservableReader.PollingMode getPollingMode() {
    return currentPollingMode;
  }

  /**
   * (package-private)<br>
   * Changes the state of the state machine
   *
   * @param stateId : new stateId
   */
  void switchState(AbstractObservableState.MonitoringState stateId) {
    this.stateService.switchState(stateId);
  }

  /**
   * (package-private)<br>
   * Get the current monitoring state
   *
   * @return current getMonitoringState
   */
  AbstractObservableState.MonitoringState getCurrentMonitoringState() {
    return this.stateService.getCurrentMonitoringState();
  }

  /**
   * thread safe method to communicate an internal event to this reader Use this method to inform
   * the reader of external event like a tag discovered or a card inserted
   *
   * @param event internal event
   */
  protected void onEvent(InternalEvent event) {
    this.stateService.onEvent(event);
  }

  /** {@inheritDoc} */
  @Override
  public final void finalizeSeProcessing() {
    if (logger.isTraceEnabled()) {
      logger.trace("[{}] start removal sequence of the reader", getName());
    }
    this.stateService.onEvent(InternalEvent.SE_PROCESSED);
  }
}
