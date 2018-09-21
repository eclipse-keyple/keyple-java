/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.webservice.old.rse;


import java.io.IOException;
import org.eclipse.keyple.plugin.remote_se.rse.IReaderSyncSession;
import org.eclipse.keyple.plugin.remote_se.transport.json.JsonParser;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclise.keyple.example.remote.webservice.HttpHelper;
import org.eclise.keyple.example.remote.webservice.old.nse.WsRseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class WsReaderClient implements IReaderSyncSession {

    private static final Logger logger = LoggerFactory.getLogger(WsRseClient.class);

    String transmitUrl;
    String sessionId;

    public WsReaderClient(String transmitUrl, String sessionId) {
        this.transmitUrl = transmitUrl;
        this.sessionId = sessionId;
    }

    public String getTransmitUrl() {
        return transmitUrl;
    }



    /**
     * Transmit SeRequestSet to Remote Secure Element
     *
     */
    public SeResponseSet transmit(SeRequestSet seRequestSet) {
        logger.info("Transmit {}", seRequestSet);

        // construct json data
        Gson parser = JsonParser.getGson();
        String data = parser.toJson(seRequestSet, SeRequestSet.class);;

        // send data to /transmit endpoint
        JsonObject response = null;
        try {
            response = HttpHelper.httpPOSTJson(HttpHelper.getConnection(transmitUrl), data);
            logger.info("Receive Response {}", response);

            // parse response to get sessionId

            SeResponseSet seResponseSet = parser.fromJson(response, SeResponseSet.class);
            return seResponseSet;

        } catch (IOException e) {
            e.printStackTrace();
            return null;// todo
        }


    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public Boolean isAsync() {
        return false;
    }

}
