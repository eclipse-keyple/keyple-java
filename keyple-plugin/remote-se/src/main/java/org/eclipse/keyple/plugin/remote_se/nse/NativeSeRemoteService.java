/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.remote_se.nse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.keyple.plugin.remote_se.transport.*;
import org.eclipse.keyple.plugin.remote_se.transport.json.SeProxyJsonParser;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.UnexpectedReaderException;
import org.eclipse.keyple.seproxy.plugin.AbstractObservableReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class NativeSeRemoteService implements NseAPI, RseClient,DtoReceiver {

    private static final Logger logger = LoggerFactory.getLogger(NativeSeRemoteService.class);

    private TransportNode transportNode;
    private SeProxyService seProxyService;
    private NseSessionManager nseSessionManager;

    public NativeSeRemoteService() {
        this.seProxyService = SeProxyService.getInstance();//todo make this as a service?
        this.nseSessionManager = new NseSessionManager();
    }

    @Override
    public TransportDTO onDTO(TransportDTO dto) {

        KeypleDTO msg = dto.getKeypleDTO();

        logger.debug("onDto {}",KeypleDTOHelper.toJson(msg));

        //receive a response to a reader_connect
        if (msg.getAction().equals(KeypleDTOHelper.READER_CONNECT) && !msg.isRequest()) {
            // parse response
            JsonObject body = SeProxyJsonParser.getGson().fromJson(msg.getBody(), JsonObject.class);
            String sessionId = msg.getSessionId();
            Integer statusCode = body.get("statusCode").getAsInt();
            String nativeReaderName = body.get("nativeReaderName").getAsString();

            //reader connection was a success
            if(statusCode == 0){
                try {
                    //observe reader
                    ProxyReader localReader = this.findLocalReader(nativeReaderName);
                    if(localReader instanceof AbstractObservableReader){
                        ((AbstractObservableReader) localReader).addObserver(this);
                    }
                    //store sessionId
                    nseSessionManager.addNewSession(sessionId, localReader.getName());
                }catch (UnexpectedReaderException e){
                    logger.warn("While receiving a confirmation of Rse connection, local reader were not found");
                }
            }else {
                logger.warn("Receive a error statusCode {} {}",statusCode, KeypleDTOHelper.toJson(msg));
            }

            return dto.nextTransportDTO(KeypleDTOHelper.NoResponse());

        }else if (msg.getAction().equals(KeypleDTOHelper.READER_TRANSMIT)) {
            SeRequestSet seRequestSet =
                    SeProxyJsonParser.getGson().fromJson(msg.getBody(), SeRequestSet.class);
            SeResponseSet seResponseSet = null;
            try {
                seResponseSet = this.onTransmit(msg.getSessionId(), seRequestSet);
            } catch (IOReaderException e) {
                e.printStackTrace();
            }
            String parseBody = SeProxyJsonParser.getGson().toJson(seResponseSet, SeResponseSet.class);
            return dto.nextTransportDTO(new KeypleDTO( msg.getAction(), parseBody, false,msg.getSessionId()));
        }else{
            logger.warn("Receive uncoregnized message action", msg.getAction());
            return dto.nextTransportDTO(KeypleDTOHelper.NoResponse());
        }
    }

    // RseClient
    @Override
    public void update(ReaderEvent event) {
        logger.info("update Reader Event {}", event.getEventType());

        //retrieve last sessionId known for this reader
        String sessionId = nseSessionManager.getLastSession(event.getReaderName());

        // construct json data
        String data = SeProxyJsonParser.getGson().toJson(event);

        transportNode.sendDTO(new KeypleDTO(KeypleDTOHelper.READER_EVENT, data, true, sessionId));

    }

    // RseClient
    @Override
    public void connectReader(ProxyReader localReader, Map<String, Object> options) {
        logger.info("connectReader {} {}", localReader, options);

        Boolean isAsync = (Boolean) options.get("isAsync");
        String transmitUrl = (String) options.get("transmitUrl");
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("nativeReaderName", new JsonPrimitive(localReader.getName()));
        jsonObject.add("isAsync", new JsonPrimitive(isAsync));
        //if (isAsync) {
        //    jsonObject.add("transmitUrl", new JsonPrimitive(transmitUrl));
        //}
        String data = jsonObject.toString();

        transportNode.sendDTO(new KeypleDTO(KeypleDTOHelper.READER_CONNECT, data, true));

    }

    // RseClient
    @Override
    public void disconnectReader(ProxyReader localReader) {
        //todo
    }



    // NseAPI
    @Override
    public SeResponseSet onTransmit(String sessionId, SeRequestSet req) throws IOReaderException {
        try {
            ProxyReader reader = findLocalReader(nseSessionManager.findReaderNameBySession(sessionId));
            return reader.transmit(req);
        } catch (UnexpectedReaderException e) {
            e.printStackTrace();
            return new SeResponseSet(new ArrayList<SeResponse>());//todo
        }
    }

    public void bind(TransportNode node){
        this.transportNode = node;
        node.setDtoReceiver(this);
    }

    public ProxyReader findLocalReader(String readerName) throws UnexpectedReaderException{
        logger.debug("Find local reader by name {} in {} plugin(s)", readerName, seProxyService.getPlugins().size());
        for(ReaderPlugin plugin : seProxyService.getPlugins()){
            try {
                return plugin.getReader(readerName);
            }catch (UnexpectedReaderException e){
                //continue
            }
        }
        throw new UnexpectedReaderException("Local Reader not found");
    }


}
