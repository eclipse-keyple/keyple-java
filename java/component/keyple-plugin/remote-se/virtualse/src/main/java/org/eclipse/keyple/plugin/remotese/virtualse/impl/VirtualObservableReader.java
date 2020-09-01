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
package org.eclipse.keyple.plugin.remotese.virtualse.impl;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.plugin.reader.ObservableReaderNotifier;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remotese.core.impl.AbstractKeypleNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (package-private)<br>
 * Observable Virtual Reader
 *
 * <p>This object is a {@link AbstractVirtualReader} with additional ObservableReader features.
 */
final class VirtualObservableReader extends AbstractVirtualReader
    implements ObservableReaderNotifier {

  private static final Logger logger = LoggerFactory.getLogger(VirtualObservableReader.class);

  /* The observers of this object */
  private final List<ReaderObserver> observers;

  /**
   * (package-private)<br>
   * Constructor
   *
   * @param pluginName The name of the plugin (must be not null).
   * @param nativeReaderName The name of the native reader (must be not null).
   * @param node The associated node (must be not null).
   */
  VirtualObservableReader(String pluginName, String nativeReaderName, AbstractKeypleNode node) {
    super(pluginName, nativeReaderName, node);
    observers = new ArrayList<ReaderObserver>();
  }

  @Override
  public void notifyObservers(ReaderEvent event) {
    if (logger.isTraceEnabled()) {
      logger.trace(
          "[{}] Notifying a reader event to {} observers. EVENTNAME = {}",
          this.getName(),
          this.countObservers(),
          event.getEventType().getName());
    }

    List<ReaderObserver> observersCopy = new ArrayList<ReaderObserver>(observers);

    for (ObservableReader.ReaderObserver observer : observersCopy) {
      observer.update(event);
    }
  }

  @Override
  public void addObserver(ReaderObserver observer) {
    Assert.getInstance().notNull(observer, "Reader Observer");

    if (observers.add(observer) && logger.isTraceEnabled()) {
      logger.trace(
          "[{}] Added reader observer '{}'", getName(), observer.getClass().getSimpleName());
    }
    ;
  }

  @Override
  public void removeObserver(ReaderObserver observer) {
    Assert.getInstance().notNull(observer, "Reader Observer");
    if (observers.remove(observer) && logger.isTraceEnabled()) {
      logger.trace(
          "[{}] Deleted reader observer '{}'", this.getName(), observer.getClass().getSimpleName());
    }
  }

  @Override
  public void clearObservers() {
    observers.clear();
    if (logger.isTraceEnabled()) {
      logger.trace("[{}] Clear reader observers", this.getName());
    }
  }

  @Override
  public int countObservers() {
    return observers.size();
  }

  @Override
  public void startSeDetection(PollingMode pollingMode) {
    Assert.getInstance().notNull(pollingMode, "Polling Mode");
    JsonObject body = new JsonObject();

    body.addProperty("pollingMode", KeypleJsonParser.getParser().toJson(pollingMode));

    sendRequest(KeypleMessageDto.Action.START_SE_DETECTION, body);
  }

  @Override
  public void stopSeDetection() {
    sendRequest(KeypleMessageDto.Action.STOP_SE_DETECTION, null);
  }

  @Override
  public void setDefaultSelectionRequest(
      AbstractDefaultSelectionsRequest defaultSelectionsRequest,
      NotificationMode notificationMode) {
    setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode, null);
  }

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
        KeypleJsonParser.getParser().toJsonTree(defaultSelectionsRequest));

    body.addProperty("notificationMode", KeypleJsonParser.getParser().toJson(notificationMode));

    if (pollingMode != null) {
      body.addProperty("pollingMode", KeypleJsonParser.getParser().toJson(pollingMode));
    }

    sendRequest(KeypleMessageDto.Action.SET_DEFAULT_SELECTION, body);
  }

  @Override
  public void finalizeSeProcessing() {
    sendRequest(KeypleMessageDto.Action.FINALIZE_SE_PROCESSING, null);
  }
}
