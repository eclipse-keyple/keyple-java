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
package org.eclipse.keyple.distributed.impl;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.eclipse.keyple.core.plugin.ObservableReaderNotifier;
import org.eclipse.keyple.core.service.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.core.util.json.KeypleGsonParser;
import org.eclipse.keyple.distributed.MessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (package-private)<br>
 * Observable Remote Reader Implementation
 *
 * <p>This object is a {@link AbstractRemoteReader} with additional ObservableReader features.
 *
 * @since 1.0
 */
final class ObservableRemoteReaderImpl extends AbstractRemoteReader
    implements ObservableReaderNotifier {

  private static final Logger logger = LoggerFactory.getLogger(ObservableRemoteReaderImpl.class);

  private final List<ReaderObserver> observers;
  private final ExecutorService eventNotificationPool;

  /**
   * (package-private)<br>
   *
   * @param pluginName The name of the plugin (must be not null).
   * @param localReaderName The name of the local reader (must be not null).
   * @param node The associated node (must be not null).
   * @param sessionId Associated session Id (can be null)
   * @param clientNodeId Associated client node Id (can be null)
   * @param eventNotificationPool The thread pool used to notify ReaderEvent (must be not null).
   * @since 1.0
   */
  ObservableRemoteReaderImpl(
      String pluginName,
      String localReaderName,
      AbstractNode node,
      String sessionId,
      String clientNodeId,
      ExecutorService eventNotificationPool) {
    super(pluginName, localReaderName, node, sessionId, clientNodeId);
    this.observers = new ArrayList<ReaderObserver>();
    this.eventNotificationPool = eventNotificationPool;
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void notifyObservers(final ReaderEvent event) {
    if (logger.isTraceEnabled()) {
      logger.trace(
          "[{}] Notifying a reader event to {} observers. EVENTNAME = {}",
          this.getName(),
          this.countObservers(),
          event.getEventType().name());
    }

    List<ReaderObserver> observersCopy = new ArrayList<ReaderObserver>(observers);

    /* Notify each observer of the readerEvent in a separate thread */
    for (final ObservableReader.ReaderObserver observer : observersCopy) {
      eventNotificationPool.execute(
          new Runnable() {
            @Override
            public void run() {
              observer.update(event);
            }
          });
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void addObserver(ReaderObserver observer) {
    Assert.getInstance().notNull(observer, "Reader Observer");

    if (observers.add(observer) && logger.isTraceEnabled()) {
      logger.trace(
          "[{}] Added reader observer '{}'", getName(), observer.getClass().getSimpleName());
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void removeObserver(ReaderObserver observer) {
    Assert.getInstance().notNull(observer, "Reader Observer");
    if (observers.remove(observer) && logger.isTraceEnabled()) {
      logger.trace(
          "[{}] Deleted reader observer '{}'", this.getName(), observer.getClass().getSimpleName());
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void clearObservers() {
    observers.clear();
    if (logger.isTraceEnabled()) {
      logger.trace("[{}] Clear reader observers", this.getName());
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public int countObservers() {
    return observers.size();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void startCardDetection(PollingMode pollingMode) {
    Assert.getInstance().notNull(pollingMode, "Polling Mode");
    JsonObject body = new JsonObject();

    body.addProperty("pollingMode", pollingMode.name());

    sendRequest(MessageDto.Action.START_CARD_DETECTION, body);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void stopCardDetection() {
    sendRequest(MessageDto.Action.STOP_CARD_DETECTION, null);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void setDefaultSelectionRequest(
      AbstractDefaultSelectionsRequest defaultSelectionsRequest,
      NotificationMode notificationMode) {
    setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode, null);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void setDefaultSelectionRequest(
      AbstractDefaultSelectionsRequest defaultSelectionsRequest,
      NotificationMode notificationMode,
      PollingMode pollingMode) {
    Assert.getInstance()
        .notNull(defaultSelectionsRequest, "Default Selections Request")
        .notNull(notificationMode, "Notification Mode");

    // Extract info from the message
    JsonObject body = new JsonObject();

    body.add(
        "defaultSelectionsRequest",
        KeypleGsonParser.getParser().toJsonTree(defaultSelectionsRequest));

    body.addProperty("notificationMode", notificationMode.name());

    if (pollingMode != null) {
      body.addProperty("pollingMode", pollingMode.name());
    }

    sendRequest(MessageDto.Action.SET_DEFAULT_SELECTION, body);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void finalizeCardProcessing() {
    sendRequest(MessageDto.Action.FINALIZE_CARD_PROCESSING, null);
  }
}
