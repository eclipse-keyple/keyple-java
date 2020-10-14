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
  private final ServerVirtualObservableReader masterReader;

  /**
   * (package-private)<br>
   * Constructor
   *
   * @param reader The reader to decorate (must be not null).
   * @param serviceId The service id (nullable only if this instance is a slave reader).
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
    this.masterReader = masterReader;
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void notifyObservers(ReaderEvent event) {
    if (masterReader != null) {
      masterReader.notifyObservers(event);
    } else {
      reader.notifyObservers(event);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void addObserver(ReaderObserver observer) {
    Assert.getInstance().notNull(observer, "observer");
    if (masterReader != null) {
      masterReader.addObserver(observer);
    } else {
      reader.addObserver(observer);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void removeObserver(ReaderObserver observer) {
    Assert.getInstance().notNull(observer, "observer");
    if (masterReader != null) {
      masterReader.removeObserver(observer);
    } else {
      reader.removeObserver(observer);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void clearObservers() {
    if (masterReader != null) {
      masterReader.clearObservers();
    } else {
      reader.clearObservers();
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public int countObservers() {
    if (masterReader != null) {
      return masterReader.countObservers();
    } else {
      return reader.countObservers();
    }
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

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public boolean isContactless() {
    return reader.isContactless();
  }

  /**
   * (package-private)<br>
   * Return the master reader if any, null if none
   *
   * @return nullable instance of a master reader
   */
  ServerVirtualObservableReader getMasterReader() {
    return masterReader;
  }
}
