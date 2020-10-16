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
package org.eclipse.keyple.plugin.remotese.pluginse;

import java.util.*;
import org.eclipse.keyple.core.plugin.reader.ObservableReaderNotifier;
import org.eclipse.keyple.core.reader.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.reader.event.ObservableReader;
import org.eclipse.keyple.core.reader.event.ReaderEvent;
import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.pluginse.method.RmSetDefaultSelectionRequestTx;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodTxEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Observable Virtual Reader
 *
 * <p>add Observable methods to VirtualReaderImpl
 */
final class VirtualObservableReaderImpl extends VirtualReaderImpl
    implements VirtualObservableReader, ObservableReaderNotifier {

  private static final Logger logger = LoggerFactory.getLogger(VirtualObservableReaderImpl.class);

  /* The observers of this object */
  private List<ReaderObserver> observers;
  /*
   * this object will be used to synchronize the access to the observers list in order to be
   * thread safe
   */
  private final Object sync = new Object();

  public VirtualObservableReaderImpl(
      VirtualReaderSession session,
      String nativeReaderName,
      RemoteMethodTxEngine rmTxEngine,
      String slaveNodeId,
      boolean isContactless,
      Map<String, String> options) {
    super(session, nativeReaderName, rmTxEngine, slaveNodeId, isContactless, options);
  }

  @Override
  public final void addObserver(final ObservableReader.ReaderObserver observer) {
    if (observer == null) {
      return;
    }

    logger.trace(
        "Adding '{}' as an observer of '{}'.", observer.getClass().getSimpleName(), getName());

    synchronized (sync) {
      if (observers == null) {
        observers = new ArrayList<ReaderObserver>(1);
      }
      observers.add(observer);
    }
  }

  @Override
  public final void removeObserver(final ObservableReader.ReaderObserver observer) {
    if (observer == null) {
      return;
    }

    logger.trace("[{}] Deleting a reader observer", this.getName());

    synchronized (sync) {
      if (observers != null) {
        observers.remove(observer);
      }
    }
  }

  @Override
  public final void notifyObservers(final ReaderEvent event) {

    logger.trace(
        "[{}] Notifying a reader event to {} observers. EVENTNAME = {}",
        this.getName(),
        this.countObservers(),
        event.getEventType().name());

    List<ReaderObserver> observersCopy;

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

  @Override
  public final int countObservers() {
    return observers == null ? 0 : observers.size();
  }

  @Override
  public final void clearObservers() {
    if (observers != null) {
      this.observers.clear();
    }
  }

  @Override
  public void startCardDetection(PollingMode pollingMode) {
    logger.warn(
        "startCardDetection is not implemented in VirtualObservableReaderImpl, please use the method on the slave node");
  }

  @Override
  public void stopCardDetection() {
    logger.warn(
        "stopCardDetection is not implemented in VirtualObservableReaderImpl, please use the method on the slave node");
  }

  /*
   * PACKAGE PRIVATE
   */

  /**
   * When an event occurs on the Remote LocalReader, notify Observers
   *
   * @param event
   */
  void onRemoteReaderEvent(final ReaderEvent event) {

    logger.debug("{} EVENT {} ", this.getName(), event.getEventType());

    if (this.countObservers() > 0) {
      final VirtualObservableReaderImpl thisReader = this;
      // launch event another thread to permit blocking method to be used in update
      // method (such as transmit)
      rmTxEngine
          .getExecutorService()
          .execute(
              new Runnable() {
                @Override
                public void run() {
                  thisReader.notifyObservers(event);
                }
              });
    } else {
      logger.debug(
          "An event was received but no observers are declared into VirtualReader : {} {}",
          this.getName(),
          event.getEventType());
    }
  }

  @Override
  public void setDefaultSelectionRequest(
      AbstractDefaultSelectionsRequest defaultSelectionsRequest,
      NotificationMode notificationMode) {

    RmSetDefaultSelectionRequestTx setDefaultSelectionRequest =
        new RmSetDefaultSelectionRequestTx(
            defaultSelectionsRequest,
            notificationMode,
            this.getNativeReaderName(),
            this.getName(),
            this.getSession().getSessionId(),
            session.getSlaveNodeId(),
            session.getMasterNodeId());

    try {
      // blocking call
      setDefaultSelectionRequest.execute(rmTxEngine);
    } catch (KeypleRemoteException e) {
      logger.error(
          "setDefaultSelectionRequest encounters an exception while communicating with slave", e);
    }
  }

  @Override
  public void setDefaultSelectionRequest(
      AbstractDefaultSelectionsRequest defaultSelectionsRequest,
      NotificationMode notificationMode,
      PollingMode pollingMode) {

    PollingMode singleshot = PollingMode.SINGLESHOT;

    RmSetDefaultSelectionRequestTx setDefaultSelectionRequest =
        new RmSetDefaultSelectionRequestTx(
            defaultSelectionsRequest,
            notificationMode,
            singleshot,
            this.getNativeReaderName(),
            this.getName(),
            this.getSession().getSessionId(),
            session.getSlaveNodeId(),
            session.getMasterNodeId());

    try {
      // blocking call
      setDefaultSelectionRequest.execute(rmTxEngine);
    } catch (KeypleRemoteException e) {
      logger.error(
          "setDefaultSelectionRequest encounters an exception while communicating with slave", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public final void finalizeSeProcessing() {
    // TODO check why we can't test if the channel is already closed here.
    releaseChannel();
  }
}
