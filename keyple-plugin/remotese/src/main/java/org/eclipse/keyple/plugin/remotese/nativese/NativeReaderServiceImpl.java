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

import java.util.ArrayList;
import java.util.Map;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.seproxy.plugin.AbstractObservableReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;

/**
 * Native Service to manage local reader and connect them to Remote Service
 *
 */
public class NativeReaderServiceImpl implements NativeReaderService, DtoHandler {

    private static final Logger logger = LoggerFactory.getLogger(NativeReaderServiceImpl.class);

    private final DtoSender dtoSender;
    private final SeProxyService seProxyService;
    // private final NseSessionManager nseSessionManager;

    /**
     * Constructor
     * 
     * @param dtoSender : Define which DTO sender will be called when a DTO needs to be sent.
     */
    public NativeReaderServiceImpl(DtoSender dtoSender) {
        this.seProxyService = SeProxyService.getInstance();
        this.dtoSender = dtoSender;
        // this.nseSessionManager = new NseSessionManager();
    }


    /**
     * Listens to a TransportNode to dispatchDTO
     * 
     * @param node : network entry point that receives DTO
     */
    public void bindDtoEndpoint(TransportNode node) {
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

        logger.debug("onDto {}", KeypleDtoHelper.toJson(keypleDTO));

        // receive a response to a reader_connect
        if (keypleDTO.getAction().equals(KeypleDtoHelper.READER_CONNECT)
                && !keypleDTO.isRequest()) {
            logger.info("**** RESPONSE - READER_CONNECT ****");

            // parse response
            JsonObject body = JsonParser.getGson().fromJson(keypleDTO.getBody(), JsonObject.class);
            Integer statusCode = body.get("statusCode").getAsInt();
            String nativeReaderName = keypleDTO.getNativeReaderName();

            // reader connection was a success
            if (statusCode == 0) {
                try {
                    // observe reader to propagate reader events
                    ProxyReader localReader = this.findLocalReader(nativeReaderName);
                    if (localReader instanceof AbstractObservableReader) {
                        logger.debug(
                                "Add NativeReaderServiceImpl as an observer for native reader {}",
                                localReader.getName());
                        ((AbstractObservableReader) localReader).addObserver(this);
                    }
                    // todo store sessionId in reader as a parameter?
                    // nseSessionManager.addNewSession(sessionId, localReader.getName());

                } catch (KeypleReaderNotFoundException e) {
                    logger.warn(
                            "While receiving a confirmation of Rse connection, local reader was not found");
                }
            } else {
                logger.warn("Receive a error statusCode {} {}", statusCode,
                        KeypleDtoHelper.toJson(keypleDTO));
            }

            out = transportDto.nextTransportDTO(KeypleDtoHelper.ACK());

        } else if (keypleDTO.getAction().equals(KeypleDtoHelper.READER_TRANSMIT)) {
            logger.info("**** ACTION - READER_TRANSMIT ****");

            SeRequestSet seRequestSet =
                    JsonParser.getGson().fromJson(keypleDTO.getBody(), SeRequestSet.class);

            SeResponseSet seResponseSet = null;
            String nativeReaderName = keypleDTO.getNativeReaderName();
            try {
                // execute transmit
                seResponseSet =
                        this.onTransmit(nativeReaderName, keypleDTO.getSessionId(), seRequestSet);
            } catch (KeypleReaderException e) {
                e.printStackTrace();
            }
            // prepare response
            String parseBody = JsonParser.getGson().toJson(seResponseSet, SeResponseSet.class);
            out = transportDto.nextTransportDTO(new KeypleDto(keypleDTO.getAction(), parseBody,
                    false, keypleDTO.getSessionId(), nativeReaderName,
                    keypleDTO.getVirtualReaderName(), keypleDTO.getNodeId()));


        } else {

            if (KeypleDtoHelper.isACK(keypleDTO)) {
                logger.info("**** ACK ****");
            } else {
                logger.info("**** ERROR - UNRECOGNIZED ****");
                logger.error("Receive unrecognized message action : {} {} {} {}",
                        keypleDTO.getAction(), keypleDTO.getSessionId(), keypleDTO.getBody(),
                        keypleDTO.isRequest());
            }
            out = transportDto.nextTransportDTO(KeypleDtoHelper.NoResponse());
        }

        logger.debug("onDto response to be sent {}", KeypleDtoHelper.toJson(out.getKeypleDTO()));
        return out;


    }


    /**
     * Connect a local reader to Remote SE Plugin {@link NativeReaderService}
     * 
     * @param nodeId : a chosen but unique terminal id (i.e AndroidDevice2)
     * @param localReader : native reader to be connected
     * @param options : map of options, not in used at the moment
     */
    @Override
    public void connectReader(String nodeId, ProxyReader localReader, Map<String, Object> options) {
        logger.info("connectReader {} {}", localReader.getName(), options);

        JsonObject jsonObject = new JsonObject();

        String data = jsonObject.toString();

        dtoSender.sendDTO(new KeypleDto(KeypleDtoHelper.READER_CONNECT, data, true, null,
                localReader.getName(), null, nodeId));

    }

    @Override
    public void disconnectReader(String nodeId, ProxyReader localReader) {
        logger.info("disconnectReader {} {}", localReader.getName());

        // String data = jsonObject.toString();
        dtoSender.sendDTO(new KeypleDto(KeypleDtoHelper.READER_DISCONNECT, "{}", true, null,
                localReader.getName(), null, nodeId));

        // stop propagating the local reader events
        ((AbstractObservableReader) localReader).removeObserver(this);

    }


    // NseAPI
    private SeResponseSet onTransmit(String nativeReaderName, String sessionId, SeRequestSet req)
            throws KeypleReaderException {
        try {
            ProxyReader reader = findLocalReader(nativeReaderName);
            return reader.transmitSet(req);
        } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
            return new SeResponseSet(new ArrayList<SeResponse>());// todo
        }
    }

    /**
     * Internal method to find a local reader by its name across multiple plugins
     * 
     * @param nativeReaderName : name of the reader to be found
     * @return found reader if any
     * @throws KeypleReaderNotFoundException if not reader were found with this name
     */
    private ProxyReader findLocalReader(String nativeReaderName)
            throws KeypleReaderNotFoundException {
        logger.debug("Find local reader by name {} in {} plugin(s)", nativeReaderName,
                seProxyService.getPlugins().size());
        for (ReaderPlugin plugin : seProxyService.getPlugins()) {
            try {
                return plugin.getReader(nativeReaderName);
            } catch (KeypleReaderNotFoundException e) {
                // continue
            }
        }
        throw new KeypleReaderNotFoundException(nativeReaderName);
    }

    // NativeReaderService

    /**
     * Do not call this method directly This method is called by a Observable<{@link ReaderEvent}>
     * 
     * @param event event to be propagated to master device
     */
    @Override
    public void update(ReaderEvent event) {
        logger.info("update Reader Event {}", event.getEventType());

        // retrieve last sessionId known for this reader
        // String sessionId = nseSessionManager.getLastSession(event.getReaderName());

        // construct json data
        String data = JsonParser.getGson().toJson(event);

        dtoSender.sendDTO(new KeypleDto(KeypleDtoHelper.READER_EVENT, data, true, null,
                event.getReaderName(), null, this.dtoSender.getNodeId()));

    }


}
