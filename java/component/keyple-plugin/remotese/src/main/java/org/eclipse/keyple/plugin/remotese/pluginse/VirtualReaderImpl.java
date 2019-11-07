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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.event.DefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.plugin.AbstractReader;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.pluginse.method.RmSetDefaultSelectionRequestTx;
import org.eclipse.keyple.plugin.remotese.pluginse.method.RmTransmitSetTx;
import org.eclipse.keyple.plugin.remotese.pluginse.method.RmTransmitTx;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodTxEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Virtual Reader is a proxy to a Native Reader on the slave terminal Use it like a local reader,
 * all API call will be transferred to the Native Reader with a RPC session
 */
final class VirtualReaderImpl extends AbstractReader implements VirtualReader {

    private final VirtualReaderSession session;
    private final String nativeReaderName;
    private final RemoteMethodTxEngine rmTxEngine;
    private final String slaveNodeId;
    private final TransmissionMode transmissionMode;

    private static final Logger logger = LoggerFactory.getLogger(VirtualReaderImpl.class);

    private Map<String, String> parameters = new HashMap<String, String>();

    /**
     * Create a new Virtual Reader (only called by @{@link RemoteSePluginImpl})
     * 
     * @param session : session associated to the reader
     * @param nativeReaderName : native reader name on slave terminal
     * @param rmTxEngine : processor for remote method
     * @param transmissionMode : transmission mode of the native reader on slave terminal
     */
    VirtualReaderImpl(VirtualReaderSession session, String nativeReaderName,
            RemoteMethodTxEngine rmTxEngine, String slaveNodeId, TransmissionMode transmissionMode,
            Map<String, String> options) {
        super(RemoteSePluginImpl.DEFAULT_PLUGIN_NAME,
                RemoteSePluginImpl.generateReaderName(nativeReaderName, slaveNodeId));
        this.session = session;
        this.nativeReaderName = nativeReaderName;
        this.rmTxEngine = rmTxEngine;
        this.slaveNodeId = slaveNodeId;
        this.transmissionMode = transmissionMode;
        this.parameters = options;
        logger.info(
                "A new virtual reader was created with name:{}, sessionId:{}, transmissionMode:{}, options:{}",
                this.getName(), session, transmissionMode, options);
    }

    /**
     * @return the current transmission mode
     */
    public TransmissionMode getTransmissionMode() {
        return transmissionMode;
    }


    public String getNativeReaderName() {
        return nativeReaderName;
    }

    public VirtualReaderSession getSession() {
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
     * @param seRequestSet : Set of SeRequest to be transmitted to SE
     * @param multiSeRequestProcessing the multi se processing mode
     * @param channelControl indicates if the channel has to be closed at the end of the processing
     * @return List of SeResponse from SE
     * @throws IllegalArgumentException
     * @throws KeypleReaderException
     */
    @Override
    protected List<SeResponse> processSeRequestSet(Set<SeRequest> seRequestSet,
            MultiSeRequestProcessing multiSeRequestProcessing, ChannelControl channelControl)
            throws IllegalArgumentException, KeypleReaderException {

        RmTransmitSetTx transmit = new RmTransmitSetTx(seRequestSet, session.getSessionId(),
                this.getNativeReaderName(), this.getName(), session.getMasterNodeId(),
                session.getSlaveNodeId());
        try {
            rmTxEngine.add(transmit);

            // blocking call
            return transmit.getResponse();
        } catch (KeypleRemoteException e) {
            if (e.getCause() != null) {
                // KeypleReaderException is inside the KeypleRemoteException
                throw (KeypleReaderException) e.getCause();

            } else {
                // create a new KeypleReaderException
                throw new KeypleReaderException(e.getMessage());
            }
        }
    }

    /**
     * Blocking Transmit
     * 
     * @param seRequest : SeRequest to be transmitted to SE
     * @param channelControl indicates if the channel has to be closed at the end of the processing
     * @return seResponse : SeResponse from SE
     * @throws IllegalArgumentException
     * @throws KeypleReaderException
     */
    @Override
    protected SeResponse processSeRequest(SeRequest seRequest, ChannelControl channelControl)
            throws IllegalArgumentException, KeypleReaderException {

        RmTransmitTx transmit =
                new RmTransmitTx(seRequest, session.getSessionId(), this.getNativeReaderName(),
                        this.getName(), session.getMasterNodeId(), session.getSlaveNodeId());
        try {
            rmTxEngine.add(transmit);

            // blocking call
            return transmit.getResponse();
        } catch (KeypleRemoteException e) {
            e.printStackTrace();
            throw (KeypleReaderException) e.getCause();
        }

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
        final VirtualReaderImpl thisReader = this;

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
        return parameters;
    }

    @Override
    public void setParameter(String key, String value) throws IllegalArgumentException {
        parameters.put(key, value);
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
        // TODO implement this method
        // ensure that only one exchange with the remote part is necessary
    }
}
