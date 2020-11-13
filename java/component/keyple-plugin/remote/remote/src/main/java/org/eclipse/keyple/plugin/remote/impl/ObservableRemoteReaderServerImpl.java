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
package org.eclipse.keyple.plugin.remote.impl;

import org.eclipse.keyple.core.plugin.reader.ObservableReaderNotifier;
import org.eclipse.keyple.core.service.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.plugin.remote.ObservableRemoteReaderServer;

/**
 * (package-private)<br>
 * Observable Remote Reader Server Implementation.<br>
 * This object is a decorator of a {@link ObservableRemoteReaderImpl}.
 */
final class ObservableRemoteReaderServerImpl extends AbstractRemoteReaderServer
    implements ObservableRemoteReaderServer, ObservableReaderNotifier {

  private final ObservableRemoteReaderImpl reader;
  private final ObservableRemoteReaderServerImpl masterReader;

  /**
   * (package-private)<br>
   * Constructor
   *
   * @param reader The reader to decorate (must be not null).
   * @param serviceId The service id (nullable only if this instance is a slave reader).
   * @param userInputDataJson The user input data as a JSON string (optional).
   * @param initialCardContentJson The initial card content as a JSON string (optional).
   */
  ObservableRemoteReaderServerImpl(
      ObservableRemoteReaderImpl reader,
      String serviceId,
      String userInputDataJson,
      String initialCardContentJson,
      ObservableRemoteReaderServerImpl masterReader) {
    super(reader, serviceId, userInputDataJson, initialCardContentJson);
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
  public void startCardDetection(PollingMode pollingMode) {
    reader.startCardDetection(pollingMode);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void stopCardDetection() {
    reader.stopCardDetection();
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
  public void finalizeCardProcessing() {
    reader.finalizeCardProcessing();
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
  ObservableRemoteReaderServerImpl getMasterReader() {
    return masterReader;
  }
}
