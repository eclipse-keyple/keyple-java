/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.webservice.webservice.nse;


import java.io.IOException;
import java.io.OutputStream;
import org.eclipse.keyple.plugin.remote_se.transport.json.SeProxyJsonParser;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclise.keyple.example.remote.webservice.common.HttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Endpoint "/transmit" executes seRequestSet on the native SE Reader
 */
public class NseEndpoint implements HttpHandler {

    ProxyReader reader; // todo should be stateless?

    private static final Logger logger = LoggerFactory.getLogger(NseEndpoint.class);

    public NseEndpoint(ProxyReader reader) {
        this.reader = reader;
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

        if (requestMethod.equals("POST")) {
            // connect a new reader
            onTransmit(t);

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
    private void onTransmit(HttpExchange t) throws IOException {
        // parse body
        String body = HttpHelper.parseBodyToString(t.getRequestBody());// .. parse the request body

        logger.debug("Parse SeRequestSet from body {} ", body);
        SeRequestSet seRequestSet = SeProxyJsonParser.getGson().fromJson(body, SeRequestSet.class);

        // connect reader with readerName
        logger.debug("Transmit SeRequestSet to local reader : {} ", reader.getName());
        SeResponseSet seResponseSet = reader.transmit(seRequestSet);

        // return response
        String responseBody =
                SeProxyJsonParser.getGson().toJson(seResponseSet, SeResponseSet.class);

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
