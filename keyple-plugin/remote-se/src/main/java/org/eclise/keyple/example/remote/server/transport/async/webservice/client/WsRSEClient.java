/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.server.transport.async.webservice.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclise.keyple.example.remote.server.transport.RSEClient;
import org.eclise.keyple.example.remote.server.serializer.json.SeProxyJsonParser;
import org.eclise.keyple.example.remote.server.transport.async.webservice.common.HttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WsRSEClient implements RSEClient {

    private static final Logger logger = LoggerFactory.getLogger(WsRSEClient.class);

    String serverUrl;
    ProxyReader localReader;
    String sessionId;


    public WsRSEClient(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    /**
     * Connect physical localReader to RSE Plugin
     * @param localReader
     * @return sessionId
     */
    @Override
    public String connectReader(ProxyReader localReader) throws IOException{
        logger.info("Send connect Reader event {}",localReader.getName());

        //construct json data
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("localReaderName", new JsonPrimitive(localReader.getName()));
        String data = jsonObject.toString();

        //send data to /plugin endpoint
        JsonObject response = HttpHelper.httpPUTJSON(getConnection(serverUrl + HttpHelper.PLUGIN_ENDPOINT), data);
        logger.info("Receive Response {}",response);

        //parse response to get sessionId
        Gson gson = SeProxyJsonParser.getGson();
        gson.fromJson(response, JsonObject.class);
        String sessionId = gson.fromJson(response, JsonObject.class).get("sessionId").getAsString();

        //set localReader
        this.localReader = localReader;
        this.sessionId = sessionId;

        return this.sessionId;

    }

    /**
     * From ObservableReader.ReaderObserver
     * Send Event to RSE Plugin
     * @param event
     */
    @Override
    public void update(ReaderEvent event) {
        logger.info("Send Reader Event {}",event.getEventType());

        try {
            //construct json data
            JsonObject jsonObject = new JsonObject();

            jsonObject.add("pluginName", new JsonPrimitive(event.getPluginName()));
            jsonObject.add("readerName", new JsonPrimitive(event.getReaderName()));
            jsonObject.add("eventType", new JsonPrimitive(event.getEventType().toString()));
            jsonObject.add("sessionId", new JsonPrimitive(this.sessionId));

            String data = jsonObject.toString();

            //send data to /plugin endpoint
            JsonObject response = HttpHelper.httpPOSTJson(getConnection(serverUrl + HttpHelper.PLUGIN_ENDPOINT), data);

            //parse response
            processResponse(response);


        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Process response from RSE Server
     * Can contain SeRequestSet, then a SeResponseSet is sent to RSE Reader
     * @param object
     * @throws IOReaderException
     */
    public void processResponse(JsonObject object) throws IOReaderException {
        //if seResquestSet is present in the response, execute it and send back response to ticketing app
        if(SeProxyJsonParser.isSeRequestSet(object)){
            logger.debug("seRequestSet to process {}", object);

            SeRequestSet seRequestSet = SeProxyJsonParser.getGson().fromJson(object, SeRequestSet.class);
            SeResponseSet responseSet = localReader.transmit(seRequestSet);

            String data = SeProxyJsonParser.getGson().toJson(responseSet);

            try {
                //send SeResponseSet to /reader endpoint
                JsonObject responseBody = HttpHelper.httpPOSTJson(getConnection(serverUrl + HttpHelper.READER_ENDPOINT), data);

                //Loop, if there is more SeRequestSet
                processResponse(responseBody);

            } catch (IOException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }

        }else{
            logger.debug("No seRequestSet to process ");
        }
    }

    private static HttpURLConnection getConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        return (HttpURLConnection) url.openConnection();
    }




}
