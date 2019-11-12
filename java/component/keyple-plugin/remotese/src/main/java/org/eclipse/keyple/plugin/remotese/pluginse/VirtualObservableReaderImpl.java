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

import java.util.Map;
import org.eclipse.keyple.core.seproxy.event.DefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.pluginse.method.RmSetDefaultSelectionRequestTx;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodTxEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Virtual Reader is a proxy to a Native Reader on the slave terminal Use it like a local reader,
 * all API call will be transferred to the Native Reader with a RPC session
 */
final class VirtualObservableReaderImpl extends VirtualReaderImpl
        implements VirtualObservableReader {

    private static final Logger logger = LoggerFactory.getLogger(VirtualObservableReaderImpl.class);


    public VirtualObservableReaderImpl(VirtualReaderSession session, String nativeReaderName,
            RemoteMethodTxEngine rmTxEngine, String slaveNodeId, TransmissionMode transmissionMode,
            Map<String, String> options) {
        super(session, nativeReaderName, rmTxEngine, slaveNodeId, transmissionMode, options);
    }


    @Override
    public void startSeDetection(PollingMode pollingMode) {
        // TODO implement this method
    }

    @Override
    public void stopSeDetection() {
        // TODO implement this method
    }

    @Override
    public void setDefaultSelectionRequest(DefaultSelectionsRequest defaultSelectionsRequest,
            NotificationMode notificationMode) {

        RmSetDefaultSelectionRequestTx setDefaultSelectionRequest =
                new RmSetDefaultSelectionRequestTx(defaultSelectionsRequest, notificationMode,
                        this.getNativeReaderName(), this.getName(),
                        this.getSession().getSessionId(), session.getSlaveNodeId(),
                        session.getMasterNodeId());

        try {
            rmTxEngine.add(setDefaultSelectionRequest);

            // blocking call
            setDefaultSelectionRequest.getResponse();
        } catch (KeypleRemoteException e) {
            logger.error(
                    "setDefaultSelectionRequest encounters an exception while communicating with slave",
                    e);
        }


    }

    @Override
    public void setDefaultSelectionRequest(DefaultSelectionsRequest defaultSelectionsRequest,
            NotificationMode notificationMode, PollingMode pollingMode) {

        PollingMode singleshot = PollingMode.SINGLESHOT;

        RmSetDefaultSelectionRequestTx setDefaultSelectionRequest =
                new RmSetDefaultSelectionRequestTx(defaultSelectionsRequest, notificationMode,
                        singleshot, this.getNativeReaderName(), this.getName(),
                        this.getSession().getSessionId(), session.getSlaveNodeId(),
                        session.getMasterNodeId());

        try {
            rmTxEngine.add(setDefaultSelectionRequest);

            // blocking call
            setDefaultSelectionRequest.getResponse();
        } catch (KeypleRemoteException e) {
            logger.error(
                    "setDefaultSelectionRequest encounters an exception while communicating with slave",
                    e);
        }
    }
}
