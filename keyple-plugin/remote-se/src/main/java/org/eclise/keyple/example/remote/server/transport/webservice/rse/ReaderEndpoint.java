/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.server.transport.webservice.rse;

import java.io.IOException;
import java.io.OutputStream;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclise.keyple.example.remote.server.RsePlugin;
import org.eclise.keyple.example.remote.server.RseReader;
import org.eclise.keyple.example.remote.server.serializer.json.SeProxyJsonParser;
import org.eclise.keyple.example.remote.server.transport.SneSession;
import org.eclise.keyple.example.remote.server.transport.webservice.common.HttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


/**
 * Endpoint "/reader" Manages reader API : transmit
 */
public class ReaderEndpoint implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(PluginEndpoint.class);

    public ReaderEndpoint() {
        logger.debug("WSServerReader constructor");
    }

    private RsePlugin plugin;

    public void setPlugin(RsePlugin plugin) {
        this.plugin = plugin;
    }


    @Override
    public void handle(HttpExchange t) throws IOException {
        logger.debug("Incoming Request {} ", t.getRequestMethod());
        String requestMethod = t.getRequestMethod();

        if (requestMethod.equals("POST")) {
            // if body is seResponse
            processSeResponseSet(t);
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
    private void processSeResponseSet(HttpExchange t) throws IOException {
        // parse body
        String body = HttpHelper.parseBodyToString(t.getRequestBody());// .. parse the request body
        logger.debug("Incoming Response Body {} ", body);
        SeResponseSet seResponseSet =
                SeProxyJsonParser.getGson().fromJson(body, SeResponseSet.class);

        // todo should retrieve the matching session from reader
        RseReader reader = (RseReader) plugin.getReaders().first();
        SneSession session = (SneSession) reader.getSession();

        // notify of the arrival of the SeResponseSet
        session.asyncSetSeResponseSet(seResponseSet);

        String responseBody = null;

        // todo check is there is more seRequestSet to send
        if (session.hasSeRequestSet()) {
            responseBody = SeProxyJsonParser.getGson().toJson(session.getSeRequestSet());
        } else {
            responseBody = "{}";
        }

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
