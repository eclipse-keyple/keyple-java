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
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.plugin.remotese.virtualse.RemoteSeServerObservableReader;

/**
 * (package-private)<br>
 * Server Virtual Observable Reader class.<br>
 * This object is a decorator of a {@link VirtualObservableReader}.
 */
final class ServerVirtualObservableReader extends AbstractServerVirtualReader
    implements RemoteSeServerObservableReader, ObservableReaderNotifier {

  private final VirtualObservableReader reader;
  private final ObservableReaderNotifier masterReader;
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
    this.masterReader = masterReader == null ? this.reader : masterReader;
    this.isDelegated = masterReader == null;
  }

  /**
   * (public)<br>
   * Notify event to the master reader observers
   *
   * @param event non nullable instance of a readerEvent
   */
  @Override
  public void notifyObservers(ReaderEvent event) {
    masterReader.notifyObservers(event);
  }

  /**
   * (public)<br>
   * Add observer on the master reader
   *
   * @param observer non nullable instance of a reader observer
   */
  @Override
  public void addObserver(ReaderObserver observer) {
    Assert.getInstance().notNull(observer, "observer");
    masterReader.addObserver(observer);
  }

  /**
   * (public)<br>
   * Remove observers on the master reader. If the master reader has no observer left, unplug it
   * from the plugin
   *
   * @since 1.0
   */
  @Override
  public void removeObserver(ReaderObserver observer) {
    Assert.getInstance().notNull(observer, "observer");
    masterReader.removeObserver(observer);
    if (masterReader.countObservers() == 0) {
      // unregister the master reader
      RemoteSeServerPluginImpl.unregisterReader(
          reader.getPluginName(), masterReader.getName()); // unregister reader from plugin
      if (isDelegated) {
        // if this reader was not the master reader, unregister too
        RemoteSeServerPluginImpl.unregisterReader(
            reader.getPluginName(), this.getName()); // unregister reader from plugin
      }
    }
  }

  /**
   * Clear observers on the master reader and unplug it from the plugin
   *
   * @since 1.0
   */
  @Override
  public void clearObservers() {
    masterReader.clearObservers();
    // unregister the notifier reader (either master or delegate)
    RemoteSeServerPluginImpl.unregisterReader(
        reader.getPluginName(), masterReader.getName()); // unregister reader from plugin
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
    return reader.countObservers();
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
