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
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.seproxy.message.ProxyReader;
import org.eclipse.keyple.seproxy.plugin.AbstractObservableReader;
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

    /**
     * Constructor
     * 
     * @param dtoNode : Define which DTO sender will be called when a DTO needs to be sent.
     */
    public SlaveAPI(SeProxyService seProxyService, DtoNode dtoNode, String masterNodeId) {
        this.seProxyService = seProxyService;
        this.dtoNode = dtoNode;
        this.rmTxEngine = new RemoteMethodTxEngine(dtoNode);
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

        logger.trace("onDto {}", KeypleDtoHelper.toJson(keypleDTO));

        RemoteMethod method = RemoteMethod.get(keypleDTO.getAction());
        logger.debug("Remote Method called : {} - isRequest : {}", method, keypleDTO.isRequest());

        switch (method) {
            case READER_CONNECT:
                // process READER_CONNECT response
                if (keypleDTO.isRequest()) {
                    throw new IllegalStateException(
                            "a READER_CONNECT request has been received by SlaveAPI");
                } else {
                    // send DTO to TxEngine
                    out = this.rmTxEngine.onDTO(transportDto);
                }
                break;

            case READER_DISCONNECT:
                // process READER_DISCONNECT response
                if (keypleDTO.isRequest()) {
                    throw new IllegalStateException(
                            "a READER_DISCONNECT request has been received by SlaveAPI");
                } else {
                    // send DTO to TxEngine
                    out = this.rmTxEngine.onDTO(transportDto);
                }
                break;


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

        logger.trace("onDto response to be sent {}", KeypleDtoHelper.toJson(out.getKeypleDTO()));
        return out;


    }


    /**
     * Connect a local reader to Remote SE Plugin {@link INativeReaderService}
     * 
     * @param localReader : native reader to be connected
     */
    @Override
    public String connectReader(ProxyReader localReader) throws KeypleReaderException {

        logger.info("connectReader {} from device {}", localReader.getName(), dtoNode.getNodeId());

        RmConnectReaderTx connect = new RmConnectReaderTx(null, localReader.getName(), null,
                masterNodeId, localReader, dtoNode.getNodeId(), this);
        try {
            rmTxEngine.register(connect);
            return connect.get();
        } catch (KeypleRemoteException e) {
            throw new KeypleReaderException("An error occurred while calling connectReader", e);
        }

    }

    @Override
    public void disconnectReader(String sessionId, String nativeReaderName)
            throws KeypleReaderException {
        logger.info("disconnectReader {} from device {}", nativeReaderName, dtoNode.getNodeId());

        RmDisconnectReaderTx disconnect = new RmDisconnectReaderTx(sessionId, nativeReaderName,
                dtoNode.getNodeId(), masterNodeId);

        try {
            rmTxEngine.register(disconnect);
            disconnect.get();
            ProxyReader nativeReader = findLocalReader(nativeReaderName);
            if (nativeReader instanceof AbstractObservableReader) {
                // stop propagating the local reader events
                ((AbstractObservableReader) nativeReader).removeObserver(this);
            }
        } catch (KeypleRemoteException e) {
            throw new KeypleReaderException("An error occurred while calling connectReader", e);
        } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
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
        logger.info("SlaveAPI listens for event from native Reader - Received Event {}",
                event.getEventType());

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


}
