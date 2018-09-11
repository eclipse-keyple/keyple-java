/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.server.transport.async.webservice.server;


import java.io.*;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.UnexpectedReaderException;
import org.eclise.keyple.example.remote.server.RSEPlugin;
import org.eclise.keyple.example.remote.server.RSEReader;
import org.eclise.keyple.example.remote.server.serializer.json.SeProxyJsonParser;
import org.eclise.keyple.example.remote.server.transport.async.AsyncRSEReaderSession;
import org.eclise.keyple.example.remote.server.transport.async.webservice.common.HttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Endpoint "/plugin" Manages plugin API : onReaderConnect, onReaderEvent
 */
public class PluginEndpoint implements HttpHandler {

    RSEPlugin plugin;

    private static final Logger logger = LoggerFactory.getLogger(PluginEndpoint.class);

    public static String ENDPOINT = "/plugin";

    public PluginEndpoint() {
        logger.debug("WSServerListener constructor");
    }

    public void setPlugin(RSEPlugin plugin) {
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

        // generate sessionId
        String sessionId = String.valueOf(System.currentTimeMillis());

        // connect reader with readerName
        plugin.connectRemoteReader(readerName, new WsRSEReaderSession(sessionId));

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
            RSEReader reader = (RSEReader) plugin.getReaderByRemoteName(readerName);
            AsyncRSEReaderSession session = (AsyncRSEReaderSession) reader.getSession();

            if (session.hasSeRequestSet()) {
                responseBody = SeProxyJsonParser.getGson().toJson(session.getSeRequestSet());
            } else {
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
