package org.eclise.keyple.example.remote.server.transport.webservice.rse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclise.keyple.example.remote.server.serializer.json.SeProxyJsonParser;
import org.eclise.keyple.example.remote.server.transport.webservice.common.HttpHelper;
import org.eclise.keyple.example.remote.server.transport.NseClient;
import org.eclise.keyple.example.remote.server.transport.webservice.nse.WsRseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class WsNseClient implements NseClient {

    private static final Logger logger = LoggerFactory.getLogger(WsRseClient.class);

    String transmitUrl;
    String sessionId;

    public WsNseClient(String transmitUrl, String sessionId) {
        this.transmitUrl = transmitUrl;
        this.sessionId = sessionId;
    }

    public String getTransmitUrl(){
        return transmitUrl;
    }


    @Override
    public String getName() {
        //not implemented
        return null;
    }

    @Override
    public boolean isSePresent() {
        //not implemented
        return false;
    }

    @Override
    public void addSeProtocolSetting(SeProtocolSetting seProtocolSetting) {
        //not implemented
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
    public Boolean isDuplex() {
        return true;
    }
}
