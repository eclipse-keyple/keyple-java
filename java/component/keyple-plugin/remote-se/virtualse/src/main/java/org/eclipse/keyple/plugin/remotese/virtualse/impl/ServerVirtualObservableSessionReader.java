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
 * Server Virtual Observable Session Reader class.<br>
 * This object is a proxy of a {@link ServerVirtualObservableReader}.
 */
final class ServerVirtualObservableSessionReader extends AbstractServerVirtualReader
    implements RemoteSeServerObservableReader, ObservableReaderNotifier {

  private final ServerVirtualObservableReader masterReader;
  private final VirtualObservableReader reader;
  /**
   * (package-private)<br>
   * Constructor
   *
   * @param masterReader The reader to proxy (must be not null).
   * @param userInputDataJson The user input data as a JSON string (optional).
   * @param masterReader The master virtual observable reader
   */
  ServerVirtualObservableSessionReader(
      VirtualObservableReader reader,
      String userInputDataJson,
      ServerVirtualObservableReader masterReader) {
    super(reader, null, userInputDataJson, null);
    this.reader = reader;
    this.masterReader = masterReader;
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void notifyObservers(ReaderEvent event) {
    // proxy the message to the masterReader
    masterReader.notifyObservers(event);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void addObserver(ReaderObserver observer) {
    masterReader.addObserver(observer);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void removeObserver(ReaderObserver observer) {
    masterReader.removeObserver(observer);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void clearObservers() {
    masterReader.clearObservers();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public int countObservers() {
    return masterReader.countObservers();
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
