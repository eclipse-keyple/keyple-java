package org.eclise.keyple.example.remote.webservice.webservice.rse;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.plugin.remote_se.transport.json.SeProxyJsonParser;
import org.eclipse.keyple.plugin.remote_se.rse.ReaderSyncSession;
import org.eclise.keyple.example.remote.webservice.webservice.common.HttpHelper;
import org.eclise.keyple.example.remote.webservice.webservice.nse.WsRseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class WsReaderClient implements ReaderSyncSession {

    private static final Logger logger = LoggerFactory.getLogger(WsRseClient.class);

    String transmitUrl;
    String sessionId;

    public WsReaderClient(String transmitUrl, String sessionId) {
        this.transmitUrl = transmitUrl;
        this.sessionId = sessionId;
    }

    public String getTransmitUrl(){
        return transmitUrl;
    }



    /**
     * Transmit SeRequestSet to Remote Secure Element
     *
     */
    public SeResponseSet transmit(SeRequestSet seRequestSet) {
        logger.info("Transmit {}", seRequestSet);

        // construct json data
        Gson parser = SeProxyJsonParser.getGson();
        String data = parser.toJson(seRequestSet, SeRequestSet.class);;

        // send data to /transmit endpoint
        JsonObject response =
                null;
        try {
            response = HttpHelper.httpPOSTJson(HttpHelper.getConnection(transmitUrl), data);
            logger.info("Receive Response {}", response);

            // parse response to get sessionId

            SeResponseSet seResponseSet = parser.fromJson(response, SeResponseSet.class);
            return seResponseSet;

        } catch (IOException e) {
            e.printStackTrace();
            return null;//todo
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
