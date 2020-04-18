/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.remotese.pluginse;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.pluginse.method.RmSetDefaultSelectionRequestTx;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodTxEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Observable Virtual Reader add Observable methods to VirtualReaderImpl
 */
final class VirtualObservableReaderImpl extends VirtualReaderImpl
        implements VirtualObservableReader {

    private static final Logger logger = LoggerFactory.getLogger(VirtualObservableReaderImpl.class);

    /* The observers of this object */
    private Set<ReaderObserver> observers;
    /*
     * this object will be used to synchronize the access to the observers list in order to be
     * thread safe
     */
    private final Object sync = new Object();

    public VirtualObservableReaderImpl(VirtualReaderSession session, String nativeReaderName,
            RemoteMethodTxEngine rmTxEngine, String slaveNodeId, TransmissionMode transmissionMode,
            Map<String, String> options) {
        super(session, nativeReaderName, rmTxEngine, slaveNodeId, transmissionMode, options);
    }


    @Override
    public void addObserver(ObservableReader.ReaderObserver observer) {
        if (observer == null) {
            return;
        }

        logger.trace("Adding '{}' as an observer of '{}'.", observer.getClass().getSimpleName(),
                getName());

        synchronized (sync) {
            if (observers == null) {
                observers = new HashSet<ReaderObserver>(1);
            }
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(ObservableReader.ReaderObserver observer) {
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

        logger.trace("[{}] Notifying a reader event to {} observers. EVENTNAME = {}",
                this.getName(), this.countObservers(), event.getEventType().getName());

        Set<ObservableReader.ReaderObserver> observersCopy;

        synchronized (sync) {
            if (observers == null) {
                return;
            }
            observersCopy = new HashSet<ObservableReader.ReaderObserver>(observers);
        }

        for (ObservableReader.ReaderObserver observer : observersCopy) {
            observer.update(event);
        }
    }

    @Override
    public int countObservers() {
        return observers == null ? 0 : observers.size();
    }

    @Override
    public void clearObservers() {
        if (observers != null) {
            this.observers.clear();
        }
    }

    @Override
    public void notifySeProcessed() {
        // TODO Check why this method is empty here
    }

    @Override
    public void startSeDetection(PollingMode pollingMode) {
        logger.error(
                "startSeDetection is not implemented in VirtualObservableReaderImpl, please use the local method");
    }

    @Override
    public void stopSeDetection() {
        logger.error(
                "stopSeDetection is not implemented in VirtualObservableReaderImpl, please use the local method");
    }

    @Override
    public void setDefaultSelectionRequest(
            AbstractDefaultSelectionsRequest defaultSelectionsRequest,
            NotificationMode notificationMode) {

        RmSetDefaultSelectionRequestTx setDefaultSelectionRequest =
                new RmSetDefaultSelectionRequestTx(defaultSelectionsRequest, notificationMode,
                        this.getNativeReaderName(), this.getName(),
                        this.getSession().getSessionId(), session.getSlaveNodeId(),
                        session.getMasterNodeId());

        try {
            // blocking call
            setDefaultSelectionRequest.execute(rmTxEngine);
        } catch (KeypleRemoteException e) {
            logger.error(
                    "setDefaultSelectionRequest encounters an exception while communicating with slave",
                    e);
        }


    }

    @Override
    public void setDefaultSelectionRequest(
            AbstractDefaultSelectionsRequest defaultSelectionsRequest,
            NotificationMode notificationMode, PollingMode pollingMode) {

        PollingMode singleshot = PollingMode.SINGLESHOT;

        RmSetDefaultSelectionRequestTx setDefaultSelectionRequest =
                new RmSetDefaultSelectionRequestTx(defaultSelectionsRequest, notificationMode,
                        singleshot, this.getNativeReaderName(), this.getName(),
                        this.getSession().getSessionId(), session.getSlaveNodeId(),
                        session.getMasterNodeId());

        try {
            // blocking call
            setDefaultSelectionRequest.execute(rmTxEngine);
        } catch (KeypleRemoteException e) {
            logger.error(
                    "setDefaultSelectionRequest encounters an exception while communicating with slave",
                    e);
        }
    }
}
