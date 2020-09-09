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

import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.plugin.reader.ObservableReaderNotifier;
import org.eclipse.keyple.plugin.remotese.virtualse.RemoteSeServerObservableReader;

/**
 * (package-private)<br>
 * Server Virtual Observable Reader class.<br>
 * This object is a decorator of a {@link VirtualObservableReader}.
 */
final class ServerVirtualObservableReader extends AbstractServerVirtualReader
    implements RemoteSeServerObservableReader, ObservableReaderNotifier {

  private final VirtualObservableReader reader;
  private final ObservableReaderNotifier notifierReader;
  private final Boolean isDelegated;

  /**
   * (package-private)<br>
   * Constructor
   *
   * @param reader The reader to decorate (must be not null).
   * @param serviceId The service id (must be not null).
   * @param userInputDataJson The user input data as a JSON string (optional).
   * @param initialSeContentJson The initial SE content as a JSON string (optional).
   */
  ServerVirtualObservableReader(
      VirtualObservableReader reader,
      String serviceId,
      String userInputDataJson,
      String initialSeContentJson,
      ServerVirtualObservableReader masterReader) {
    super(reader, serviceId, userInputDataJson, initialSeContentJson);
    this.reader = reader;
    this.notifierReader = masterReader == null ? this.reader : masterReader;
    this.isDelegated = masterReader == null;
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void notifyObservers(ReaderEvent event) {
    notifierReader.notifyObservers(event);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void addObserver(ReaderObserver observer) {
    notifierReader.addObserver(observer);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void removeObserver(ReaderObserver observer) {
    notifierReader.removeObserver(observer);
    if (notifierReader.countObservers() == 0) {
      // unregister the notifier reader (either master or delegate)
      RemoteSeServerPluginImpl.unregisterReader(
          reader.getPluginName(), notifierReader.getName()); // unregister reader from plugin
      if (isDelegated) {
        // unplug this reader too
        RemoteSeServerPluginImpl.unregisterReader(
            reader.getPluginName(), this.getName()); // unregister reader from plugin
      }
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void clearObservers() {
    notifierReader.clearObservers();
    // unregister the notifier reader (either master or delegate)
    RemoteSeServerPluginImpl.unregisterReader(
        reader.getPluginName(), notifierReader.getName()); // unregister reader from plugin
    if (isDelegated) {
      // unplug this reader too
      RemoteSeServerPluginImpl.unregisterReader(
          reader.getPluginName(), this.getName()); // unregister reader from plugin
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public int countObservers() {
    return notifierReader.countObservers();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void startSeDetection(PollingMode pollingMode) {
    reader.startSeDetection(pollingMode);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void stopSeDetection() {
    reader.stopSeDetection();
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
    reader.setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode);
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
    reader.setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode, pollingMode);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void finalizeSeProcessing() {
    reader.finalizeSeProcessing();
  }
}
