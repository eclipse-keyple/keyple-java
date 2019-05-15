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
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeRequestSet;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.message.SeResponseSet;
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableReader;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.pluginse.method.RmSetDefaultSelectionRequestTx;
import org.eclipse.keyple.plugin.remotese.pluginse.method.RmTransmitTx;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodTxEngine;
import org.eclipse.keyple.plugin.remotese.transport.DtoNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Virtual Reader is a proxy to a Native Reader on the slave terminal
 */
public final class VirtualReader extends AbstractObservableReader {

    private final VirtualReaderSession session;
    private final String nativeReaderName;
    private final RemoteMethodTxEngine rmTxEngine;
    private final String slaveNodeId;

    private static final Logger logger = LoggerFactory.getLogger(VirtualReader.class);

    /**
     * Creates a new virtual reader Called by {@link RemoteSePlugin}
     * 
     * @param session Reader Session that helps communicate with {@link DtoNode}
     * @param nativeReaderName local name of the native reader on slave side
     */
    VirtualReader(VirtualReaderSession session, String nativeReaderName,
            RemoteMethodTxEngine rmTxEngine, String slaveNodeId) {
        super(RemoteSePlugin.PLUGIN_NAME,
                RemoteSePlugin.generateReaderName(nativeReaderName, slaveNodeId));
        this.session = session;
        this.nativeReaderName = nativeReaderName;
        this.rmTxEngine = rmTxEngine;
        this.slaveNodeId = slaveNodeId;
        logger.debug("A new virtual reader was created with session {}", session);
    }

    /**
     * TODO change this to handle the right transmission mode
     *
     * @return the current transmission mode
     */
    public TransmissionMode getTransmissionMode() {
        logger.error("getTransmissionMode is not implemented yet");
        return null;
    }

    /**
     * Name of the Native Reader
     *
     * @return local name of the native reader (on slave device)
     */
    public String getNativeReaderName() {
        return nativeReaderName;
    }

    VirtualReaderSession getSession() {
        return session;
    }

    RemoteMethodTxEngine getRmTxEngine() {
        return rmTxEngine;
    }


    @Override
    public boolean isSePresent() {
        logger.error("isSePresent is not implemented yet");
        return false;// not implemented
    }

    /**
     * Blocking TransmitSet
     * 
     * @param seRequestSet : SeRequestSet to be transmitted
     * @return seResponseSet : SeResponseSet
     * @throws IllegalArgumentException
     * @throws KeypleReaderException
     */
    @Override
    protected SeResponseSet processSeRequestSet(SeRequestSet seRequestSet)
            throws IllegalArgumentException, KeypleReaderException {

        RmTransmitTx transmit =
                new RmTransmitTx(seRequestSet, session.getSessionId(), this.getNativeReaderName(),
                        this.getName(), session.getMasterNodeId(), session.getSlaveNodeId());
        try {
            rmTxEngine.add(transmit);
            return transmit.getResponse();
        } catch (KeypleRemoteException e) {
            e.printStackTrace();
            throw (KeypleReaderException) e.getCause();
        }
    }

    /**
     * Blocking Transmit
     * 
     * @param seRequest
     * @return seResponse
     * @throws IllegalArgumentException
     * @throws KeypleReaderException
     */
    @Override
    protected SeResponse processSeRequest(SeRequest seRequest)
            throws IllegalArgumentException, KeypleReaderException {
        try {
            return this.processSeRequestSet(new SeRequestSet(seRequest)).getSingleResponse();
        } catch (KeypleReaderException e) {
            // throw the cause of the RemoteReaderException (a KeypleReaderException)
            throw (KeypleReaderException) e.getCause();
        }
    }

    @Override
    protected void startObservation() {
        logger.trace("startObservation is not used in this plugin");
    }

    @Override
    protected void stopObservation() {
        logger.trace("stopObservation is not used in this plugin");
    }


    @Override
    public void addSeProtocolSetting(SeProtocol seProtocol, String protocolRule) {
        logger.error("{} addSeProtocolSetting is not implemented yet", this.getName());
    }

    @Override
    public void setSeProtocolSetting(Map<SeProtocol, String> protocolSetting) {
        logger.error("setSeProtocolSetting is not implemented yet");
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
        final VirtualReader thisReader = this;

        logger.debug("{} EVENT {} ", this.getName(), event.getEventType());

        if (thisReader.countObservers() > 0) {
            thisReader.notifyObservers(event);
        } else {
            logger.debug(
                    "An event was received but no observers are declared into VirtualReader : {} {}",
                    thisReader.getName(), event.getEventType());
        }

    }


    /**
     *
     * HELPERS
     */


    @Override
    public Map<String, String> getParameters() {
        logger.error("getParameters is not implemented yet");
        return null;
    }

    @Override
    public void setParameter(String key, String value) throws IllegalArgumentException {
        logger.error("setParameter is not implemented yet");
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
            rmTxEngine.add(setDefaultSelectionRequest);
            setDefaultSelectionRequest.getResponse();
        } catch (KeypleRemoteException e) {
            logger.error(
                    "setDefaultSelectionRequest encounters an exception while communicating with slave",
                    e);
        }
    }


}
