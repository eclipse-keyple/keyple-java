/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.webservice.webservice.rse;


import java.io.*;
import org.eclipse.keyple.plugin.remote_se.rse.ReaderAsyncClientImpl;
import org.eclipse.keyple.plugin.remote_se.rse.IReaderAsyncSession;
import org.eclipse.keyple.plugin.remote_se.rse.IReaderSession;
import org.eclipse.keyple.plugin.remote_se.rse.RsePlugin;
import org.eclipse.keyple.plugin.remote_se.rse.RseReader;
import org.eclipse.keyple.plugin.remote_se.transport.json.SeProxyJsonParser;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.UnexpectedReaderException;
import org.eclise.keyple.example.remote.webservice.common.HttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Endpoint "/plugin" Manages plugin API : onReaderConnect, OnReaderEvent
 */
public class PluginEndpoint implements HttpHandler {

    RsePlugin plugin;

    private static final Logger logger = LoggerFactory.getLogger(PluginEndpoint.class);

    public PluginEndpoint() {
        logger.debug("WSServerListener constructor");
    }

    public void setPlugin(RsePlugin plugin) {
        logger.debug("setPlugin {}", plugin);
        this.plugin = plugin;
    }

    /**
     * Handle HTTP API
     * 
     * @param t
     * @throws IOException
     */
    public void handle(HttpExchange t) throws IOException {
        logger.debug("Incoming Request {} ", t.getRequestMethod());
        String requestMethod = t.getRequestMethod();

        if (requestMethod.equals("PUT")) {
            // connect a new reader
            onReaderConnect(t);

        } else if (requestMethod.equals("POST")) {
            // receive a new event
            onReaderEvent(t);

        } else {
            // unrecognized method
        }
    }



    /**
     * Connect Remote Reader API
     * 
     * @param t
     * @throws IOException
     */
    private void onReaderConnect(HttpExchange t) throws IOException {
        // parse body
        JsonObject body = HttpHelper.parseBody(t.getRequestBody());// .. parse the request body

        String readerName = body.get("localReaderName").getAsString();
        Boolean isAsync = body.get("isAsync").getAsBoolean();
        String serverUrl = body.get("transmitUrl").getAsString();

        // generate sessionId
        String sessionId = String.valueOf(System.currentTimeMillis());

        if (!isAsync) {
            // connect reader with readerName
            //NEW ARCHI
            //plugin.connectRemoteReader(readerName, new ReaderAsyncClientImpl(sessionId,null));
        } else {
            //plugin.connectRemoteReader(readerName, new WsReaderClient(serverUrl, sessionId));
        }


        // return response
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("sessionId", new JsonPrimitive(sessionId));
        String responseBody = jsonObject.toString();

        Integer responseCode = 201;
        t.getResponseHeaders().add("Content-Type", "application/json");
        t.sendResponseHeaders(responseCode, responseBody.length());
        OutputStream os = t.getResponseBody();
        os.write(responseBody.getBytes());
        os.close();
        logger.debug("Outcoming Response Code {} ", responseCode);
        logger.debug("Outcoming Response Body {} ", responseBody);
    }

    /**
     * Reader Event API
     * 
     * @param t
     * @throws IOException
     */
    private void onReaderEvent(HttpExchange t) throws IOException {
        // parse Body
        JsonObject body = HttpHelper.parseBody(t.getRequestBody());// .. parse the request body

        String pluginName = body.get("pluginName").getAsString();
        String readerName = body.get("readerName").getAsString();
        ReaderEvent.EventType eventType =
                ReaderEvent.EventType.valueOf(body.get("eventType").getAsString());
        String sessionId = body.get("sessionId").getAsString();

        // propagate event
        plugin.onReaderEvent(new ReaderEvent(pluginName, readerName, eventType), sessionId);

        String responseBody = null;

        // reader get SeRequest to transmit from Reader Session
        try {
            RseReader reader = (RseReader) plugin.getReaderByRemoteName(readerName);
            IReaderSession session = reader.getSession();

            // if session has not double way communication
            if (!session.isAsync()) {
                // there are SeRequestSet to process, attach them to the response
                if (((IReaderAsyncSession) session).hasSeRequestSet()) {
                    responseBody = SeProxyJsonParser.getGson()
                            .toJson(((IReaderAsyncSession) session).getSeRequestSet());
                } else {
                    responseBody = "{}";
                }
            } else {
                // session has a double way communication, seRequestSet will be managed by the nse
                responseBody = "{}";
            }

        } catch (UnexpectedReaderException e) {
            responseBody = "{}";
        }

        // send SeRequest to clients
        Integer responseCode = 200;
        t.getResponseHeaders().add("Content-Type", "application/json");
        t.sendResponseHeaders(responseCode, responseBody.length());
        OutputStream os = t.getResponseBody();
        os.write(responseBody.getBytes());
        os.close();
        logger.debug("Outcoming Response Code {} ", responseCode);
        logger.debug("Outcoming Response Body {} ", responseBody);
    }



}
