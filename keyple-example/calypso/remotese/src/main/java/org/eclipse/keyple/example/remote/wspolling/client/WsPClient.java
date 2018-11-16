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
package org.eclipse.keyple.example.remote.wspolling.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.eclipse.keyple.example.remote.transport.ClientNode;
import org.eclipse.keyple.example.remote.wspolling.WsPTransportDTO;
import org.eclipse.keyple.plugin.remotese.transport.DtoHandler;
import org.eclipse.keyple.plugin.remotese.transport.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.TransportDto;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Rest client, polls server, based on java.net client and multi threads
 */
public class WsPClient implements ClientNode {


    private static final Logger logger = LoggerFactory.getLogger(WsPClient.class);

    final private String keypleDtoEndpoint;
    final private String pollingEndpoint;
    final private String nodeId;
    final private String baseUrl;

    private DtoHandler dtoHandler;

    public WsPClient(String baseUrl, String keypleDtoEndpoint, String pollingEndpoint,
            String nodeId) {
        this.baseUrl = baseUrl;
        this.keypleDtoEndpoint = keypleDtoEndpoint;
        this.pollingEndpoint = pollingEndpoint;
        this.nodeId = nodeId;
    }



    public void startPollingWorker(final String nodeId) {

        logger.info("Start Polling Worker {}", nodeId);

        Thread pollThread = new Thread() {
            public void run() {
                // Boolean exit = false;
                while (true) {
                    try {
                        logger.debug("Polling clientNodeId {}", nodeId);
                        JsonObject httpResponse = httpPoll(
                                getConnection(
                                        baseUrl + pollingEndpoint + "?clientNodeId=" + nodeId),
                                "{}");
                        logger.debug("Polling for clientNodeId {} receive a httpResponse {}",
                                nodeId, httpResponse);
                        processHttpResponseDTO(httpResponse);
                    } catch (IOException e) {
                        logger.debug(
                                "Polling for clientNodeId {} didn't receive any response, send it again ");
                        // e.printStackTrace();
                    }
                }
            }
        };

        pollThread.start();

    }


    private void processHttpResponseDTO(JsonObject httpResponse) {

        // is response DTO ?
        if (KeypleDtoHelper.isKeypleDTO(httpResponse)) {

            KeypleDto responseDTO = KeypleDtoHelper.fromJsonObject(httpResponse);
            TransportDto transportDto = new WsPTransportDTO(responseDTO, this);
            // connection
            final TransportDto sendback = this.dtoHandler.onDTO(transportDto);

            // if sendBack is not a noResponse (can be a keyple request or keyple response)
            if (!KeypleDtoHelper.isNoResponse(sendback.getKeypleDTO())) {
                // send the keyple object in a new thread to avoid blocking the polling
                new Thread() {
                    @Override
                    public void run() {
                        sendDTO(sendback);
                    }
                }.start();
            }
        }

    }


    @Override
    public void sendDTO(TransportDto transportDto) {
        KeypleDto keypleDto = transportDto.getKeypleDTO();
        logger.debug("Ws Client send DTO {}", KeypleDtoHelper.toJson(keypleDto));
        if (!KeypleDtoHelper.isNoResponse(transportDto.getKeypleDTO())) {
            try {
                // send keyple dto
                JsonObject httpResponse = httpPOSTJson(getConnection(baseUrl + keypleDtoEndpoint),
                        KeypleDtoHelper.toJson(keypleDto));

                processHttpResponseDTO(httpResponse);

            } catch (IOException e) {
                e.printStackTrace();
                // todo manage exception or throw it
            }
        }
    }

    @Override
    public void sendDTO(KeypleDto message) {
        sendDTO(new WsPTransportDTO(message, null));
    }

    @Override
    public void update(KeypleDto event) {
        this.sendDTO(event);
    }


    /*
     * TransportNode
     */
    @Override
    public void setDtoHandler(DtoHandler dtoHandler) {
        this.dtoHandler = dtoHandler;
    }

    @Override
    public String getNodeId() {
        return nodeId;
    }


    @Override
    public void connect() {
        this.startPollingWorker(nodeId);
    }

    @Override
    public void disconnect() {
        // todo
    }


    private JsonObject httpPOSTJson(HttpURLConnection conn, String json) throws IOException {
        logger.trace("Url {} HTTP POST  : {} ", conn.getURL(), json);
        // Encode data
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.connect();

        OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
        osw.write(json);
        osw.flush();
        osw.close();

        conn.setConnectTimeout(70000);
        conn.setReadTimeout(70000);


        int responseCode = conn.getResponseCode();
        logger.trace("Response code {}", responseCode);
        JsonObject jsonObject = parseBody((InputStream) conn.getContent());
        logger.trace("Response {}", jsonObject);
        return jsonObject;
    }

    private HttpURLConnection getConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        return (HttpURLConnection) url.openConnection();
    }


    private JsonObject httpPoll(HttpURLConnection conn, String json) throws IOException {
        logger.trace("Url {} HTTP GET  : {} ", conn.getURL(), json);
        // Encode data
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.connect();

        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int responseCode = conn.getResponseCode();
        logger.trace("Response code {}", responseCode);
        JsonObject jsonObject = parseBody((InputStream) conn.getContent());
        logger.trace("Response {}", jsonObject);
        return jsonObject;
    }

    private JsonObject parseBody(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        Gson gson = JsonParser.getGson();
        return gson.fromJson(result, JsonObject.class);
    }

}
