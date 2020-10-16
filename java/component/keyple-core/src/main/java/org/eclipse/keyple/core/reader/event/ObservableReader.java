/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.reader.event;

import org.eclipse.keyple.core.reader.Reader;
import org.eclipse.keyple.core.reader.message.ChannelControl;

/**
 * Provides the API to observe cards in readers.
 *
 * <ul>
 *   <li>Observer management
 *   <li>Start/stop of card detection
 *   <li>Managing the default selection
 *   <li>Definition of polling and notification modes
 * </ul>
 *
 * @since 0.9
 */
public interface ObservableReader extends Reader {

  /**
   * This interface has to be implemented by reader observers.
   *
   * @since 0.9
   */
  interface ReaderObserver {

    /**
     * Called when a reader event occurs.
     *
     * <p>Note that this method is called <b>sequentially</b> on all observers.
     *
     * @param event The not null {@link ReaderEvent} containing all event information.
     * @since 0.9
     */
    void update(final ReaderEvent event);
  }

  /**
   * Indicates the expected behavior when processing a default selection.
   *
   * @since 0.9
   */
  enum NotificationMode {

    /**
     * All cards presented to readers are notified regardless of the result of the default
     * selection.
     */
    ALWAYS,
    /**
     * Only cards that have been successfully selected (logical channel open) will be notified. The
     * others will be ignored and the application will not be aware of them.
     */
    MATCHED_ONLY
  }

  /**
   * Indicates the action to be taken after processing a card.
   *
   * @since 0.9
   */
  enum PollingMode {

    /** Continue waiting for the insertion of a next card. */
    REPEATING,
    /** Stop and wait for a restart signal. */
    SINGLESHOT
  }

  /**
   * Register a new reader observer to be notified when a reader event occurs.
   *
   * <p>The provided observer will receive all the events produced by this reader (card insertion,
   * removal, etc.)
   *
   * <p>It is possible to add as many observers as necessary. They will be notified of events
   * <b>sequentially</b> in the order in which they are added.
   *
   * @param observer An observer object implementing the required interface (should be not null).
   * @since 0.9
   */
  void addObserver(final ReaderObserver observer);

  /**
   * Unregister a reader observer.
   *
   * <p>The observer will no longer receive any of the events produced by this reader.
   *
   * @param observer The observer object to be removed (should be not null).
   * @since 0.9
   */
  void removeObserver(final ReaderObserver observer);

  /**
   * Unregister all observers at once
   *
   * @since 0.9
   */
  void clearObservers();

  /**
   * Provides the current number of registered observers
   *
   * @return an int
   * @since 0.9
   */
  int countObservers();

  /**
   * Starts the card detection. Once activated, the application can be notified of the arrival of a
   * card.
   *
   * <p>The {@link PollingMode} indicates the action to be followed after processing the card: if
   * {@link PollingMode#REPEATING}, the card detection is restarted, if {@link
   * PollingMode#SINGLESHOT}, the card detection is stopped until a new call to startCardDetection
   * is made
   *
   * @param pollingMode The polling mode to use (should be not null).
   * @since 0.9
   */
  void startCardDetection(PollingMode pollingMode);

  /**
   * Stops the card detection.
   *
   * @since 0.9
   */
  void stopCardDetection();

  /**
   * Defines the default selection request to be processed when a card is inserted.
   *
   * <p>Depending on the card and the notificationMode parameter, a {@link
   * ReaderEvent.EventType#CARD_INSERTED EventType#CARD_INSERTED}, {@link
   * ReaderEvent.EventType#CARD_MATCHED EventType#CARD_MATCHED} or no event at all will be notified
   * to the application observers.
   *
   * @param defaultSelectionsRequest The default selection request to be operated (should be not
   *     null).
   * @param notificationMode The notification mode to use (should be not null).
   * @since 0.9
   */
  void setDefaultSelectionRequest(
      AbstractDefaultSelectionsRequest defaultSelectionsRequest, NotificationMode notificationMode);

  /**
   * Defines the default selection request and starts the card detection using the provided polling
   * mode.
   *
   * <p>The notification mode indicates whether a {@link ReaderEvent.EventType#CARD_INSERTED} event
   * should be notified even if the selection has failed ({@link NotificationMode#ALWAYS}) or
   * whether the card insertion should be ignored in this case ({@link
   * NotificationMode#MATCHED_ONLY}).
   *
   * <p>The polling mode indicates the action to be followed after processing the card: if {@link
   * PollingMode#REPEATING}, the card detection is restarted, if {@link PollingMode#SINGLESHOT}, the
   * card detection is stopped until a new call to * startCardDetection is made.
   *
   * @param defaultSelectionsRequest The default selection request to be operated.
   * @param notificationMode The notification mode to use (should be not null).
   * @param pollingMode The polling mode to use (should be not null).
   * @since 0.9
   */
  void setDefaultSelectionRequest(
      AbstractDefaultSelectionsRequest defaultSelectionsRequest,
      NotificationMode notificationMode,
      PollingMode pollingMode);

  /**
   * Terminates the processing of the card, in particular after an interruption by exception<br>
   * Do nothing if the channel is already closed.<br>
   * Channel closing is nominally managed by using the {@link ChannelControl#CLOSE_AFTER} flag
   * during the last transmission with the card. However, there are cases where exchanges with the
   * card are interrupted by an exception, in which case it is necessary to explicitly close the
   * channel using this method.
   *
   * @since 0.9
   */
  void finalizeSeProcessing();
}
