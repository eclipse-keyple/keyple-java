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
package org.eclipse.keyple.plugin.remotese.nativese;


import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableReader;
import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.nativese.method.*;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethod;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodExecutor;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodTxEngine;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * SlaveAPI to manage local reader and connect them to Remote Service
 *
 */
public class SlaveAPI implements INativeReaderService, DtoHandler, ObservableReader.ReaderObserver {

    private static final Logger logger = LoggerFactory.getLogger(SlaveAPI.class);

    private final DtoNode dtoNode;// bind node
    private final SeProxyService seProxyService;


    private final RemoteMethodTxEngine rmTxEngine;// rm command processor
    private final String masterNodeId;// master node id to connect to

    public static final long DEFAULT_RPC_TIMEOUT = 10000;


    /**
     * Constructor
     * 
     * @param dtoNode : Define which DTO sender will be called when a DTO needs to be sent.
     */
    public SlaveAPI(SeProxyService seProxyService, DtoNode dtoNode, String masterNodeId) {
        this.seProxyService = seProxyService;
        this.dtoNode = dtoNode;
        this.rmTxEngine = new RemoteMethodTxEngine(dtoNode, DEFAULT_RPC_TIMEOUT);
        this.masterNodeId = masterNodeId;
        this.bindDtoEndpoint(dtoNode);
    }

    /**
     * Constructor
     *
     * @param dtoNode : Define which DTO sender will be called when a DTO needs to be sent.
     */
    public SlaveAPI(SeProxyService seProxyService, DtoNode dtoNode, String masterNodeId,
            long timeout) {
        this.seProxyService = seProxyService;
        this.dtoNode = dtoNode;
        this.rmTxEngine = new RemoteMethodTxEngine(dtoNode, timeout);
        this.masterNodeId = masterNodeId;
        this.bindDtoEndpoint(dtoNode);
    }


    /**
     * HandleDTO from a DtoNode onDto() method will be called by the DtoNode
     * 
     * @param node : network entry point that receives DTO
     */
    private void bindDtoEndpoint(DtoNode node) {
        node.setDtoHandler(this);// incoming traffic
    }

    /**
     * Dispatch a Keyple DTO to the right Native Reader. {@link DtoHandler}
     * 
     * @param transportDto to be processed
     * @return Keyple DTO to be sent back
     */
    @Override
    public TransportDto onDTO(TransportDto transportDto) {

        KeypleDto keypleDTO = transportDto.getKeypleDTO();
        TransportDto out;

        logger.trace("{} onDto {}", dtoNode.getNodeId(), KeypleDtoHelper.toJson(keypleDTO));

        RemoteMethod method = RemoteMethod.get(keypleDTO.getAction());
        logger.debug("{} Remote Method called : {} - isRequest : {}", dtoNode.getNodeId(), method,
                keypleDTO.isRequest());

        switch (method) {

            /*
             * Response from Master
             */
            case READER_CONNECT:
            case READER_DISCONNECT:
                if (keypleDTO.isRequest()) {
                    throw new IllegalStateException("a " + keypleDTO.getAction()
                            + " request has been received by SlaveAPI");
                } else {
                    // send DTO to TxEngine
                    out = this.rmTxEngine.onDTO(transportDto);
                }
                break;

            /*
             * Request from Master
             */
            case READER_TRANSMIT:
                // must be a request
                if (keypleDTO.isRequest()) {
                    RemoteMethodExecutor rmTransmit = new RmTransmitExecutor(this);
                    out = rmTransmit.execute(transportDto);
                } else {
                    throw new IllegalStateException(
                            "a READER_TRANSMIT response has been received by SlaveAPI");
                }
                break;

            case READER_TRANSMIT_SET:
                // must be a request
                if (keypleDTO.isRequest()) {
                    RemoteMethodExecutor rmTransmitSet = new RmTransmitSetExecutor(this);
                    out = rmTransmitSet.execute(transportDto);
                } else {
                    throw new IllegalStateException(
                            "a READER_TRANSMIT_SET response has been received by SlaveAPI");
                }
                break;

            case DEFAULT_SELECTION_REQUEST:
                // must be a request
                if (keypleDTO.isRequest()) {
                    RmSetDefaultSelectionRequestExecutor rmSetDefaultSelectionRequest =
                            new RmSetDefaultSelectionRequestExecutor(this);
                    out = rmSetDefaultSelectionRequest.execute(transportDto);
                } else {
                    throw new IllegalStateException(
                            "a READER_TRANSMIT response has been received by SlaveAPI");
                }
                break;

            default:
                logger.warn("**** ERROR - UNRECOGNIZED ****");
                logger.warn("Receive unrecognized message action : {} {} {} {}",
                        keypleDTO.getAction(), keypleDTO.getSessionId(), keypleDTO.getBody(),
                        keypleDTO.isRequest());
                throw new IllegalStateException(
                        "a  ERROR - UNRECOGNIZED request has been received by SlaveAPI");
        }

        logger.trace("{} onDto response to be sent {}", dtoNode.getNodeId(),
                KeypleDtoHelper.toJson(out.getKeypleDTO()));
        return out;


    }


    /**
     * Connect a local reader to Remote SE Plugin {@link INativeReaderService}
     * 
     * @param localReader : native reader to be connected
     */
    @Override
    public String connectReader(SeReader localReader) throws KeypleReaderException {
        return connectReader(localReader, new HashMap<String, String>());
    }

    /**
     * Connect a local reader to Remote SE Plugin {@link INativeReaderService} with options
     *
     * @param localReader : native reader to be connected
     * @param options : options will be set as parameters of virtual reader
     */
    @Override
    public String connectReader(SeReader localReader, Map<String, String> options)
            throws KeypleReaderException {

        if (options == null) {
            options = new HashMap<String, String>();
        }

        logger.info("{} connectReader {} from device {}", dtoNode.getNodeId(),
                localReader.getName(), dtoNode.getNodeId());

        RmConnectReaderTx connect = new RmConnectReaderTx(null, localReader.getName(), null,
                masterNodeId, localReader, dtoNode.getNodeId(), this, options);
        try {
            rmTxEngine.add(connect);
            return connect.getResponse();
        } catch (KeypleRemoteException e) {
            throw new KeypleReaderException("An error occurred while calling connectReader", e);
        }

    }

    @Override
    public void disconnectReader(String sessionId, String nativeReaderName)
            throws KeypleReaderException {
        logger.info("{} disconnectReader {} from device {}", dtoNode.getNodeId(), nativeReaderName,
                dtoNode.getNodeId());

        RmDisconnectReaderTx disconnect = new RmDisconnectReaderTx(sessionId, nativeReaderName,
                dtoNode.getNodeId(), masterNodeId);

        try {
            rmTxEngine.add(disconnect);
            disconnect.getResponse();
            ProxyReader nativeReader = findLocalReader(nativeReaderName);
            if (nativeReader instanceof AbstractObservableReader) {
                // stop propagating the local reader events
                ((AbstractObservableReader) nativeReader).removeObserver(this);
            }
        } catch (KeypleRemoteException e) {
            throw new KeypleReaderException("An error occurred while calling connectReader", e);
        } catch (KeypleReaderNotFoundException e) {
            logger.warn("SlaveAPI#disconnectReader() : reader with name was not found",
                    nativeReaderName);
        }
    }

    /**
     * Internal method to find a local reader by its name across multiple plugins
     * 
     * @param nativeReaderName : name of the reader to be found
     * @return found reader if any
     * @throws KeypleReaderNotFoundException if not reader were found with this name
     */
    public ProxyReader findLocalReader(String nativeReaderName)
            throws KeypleReaderNotFoundException {
        logger.trace("Find local reader by name {} in {} plugin(s)", nativeReaderName,
                seProxyService.getPlugins().size());
        for (ReaderPlugin plugin : seProxyService.getPlugins()) {
            try {
                return (ProxyReader) plugin.getReader(nativeReaderName);
            } catch (KeypleReaderNotFoundException e) {
                // continue
            }
        }
        throw new KeypleReaderNotFoundException(nativeReaderName);
    }

    /**
     * Do not call this method directly This method is called by a
     * Observable&lt;{@link ReaderEvent}&gt;
     * 
     * @param event event to be propagated to master device
     */
    @Override
    public void update(ReaderEvent event) {
        logger.info("{} SlaveAPI - reader event {}", dtoNode.getNodeId(), event.getEventType());

        // construct json data
        String data = JsonParser.getGson().toJson(event);

        try {
            dtoNode.sendDTO(new KeypleDto(RemoteMethod.READER_EVENT.getName(), data, true, null,
                    event.getReaderName(), null, this.dtoNode.getNodeId(), masterNodeId));
        } catch (KeypleRemoteException e) {
            logger.error("Event " + event.toString()
                    + " could not be sent though Remote Service Interface", e);
        }
    }



    public RemoteMethodTxEngine getRmTxEngine() {
        return rmTxEngine;
    }

}
