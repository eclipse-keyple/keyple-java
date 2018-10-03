package org.eclise.keyple.example.remote.wspolling;

import com.google.gson.JsonObject;
import org.eclipse.keyple.plugin.remote_se.transport.*;
import org.eclise.keyple.example.remote.ws.HttpHelper;
import org.eclise.keyple.example.remote.ws.WsTransportDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class WsPClient implements TransportNode {


    private static final Logger logger = LoggerFactory.getLogger(WsPClient.class);

    private String endoint;
    private String pollingEndpoint;

    private DtoDispatcher dtoDispatcher;

    public WsPClient(String url, String pollingEndpoint) {
        this.endoint = url;
        this.pollingEndpoint = pollingEndpoint;
    }


    public void startPollingWorker(final String nodeId){
/*
            Timer timer = new Timer();
            timer.schedule(new TimerTask()
            {
                public void run()
                {

                    try {
                        JsonObject httpResponse = HttpHelper.httpPOSTJson(HttpHelper.getConnection(pollingEndpoint+"/"+nodeId), "wait");
                        processHttpResponseDTO(httpResponse);
                        startPollingWorker(nodeId);
                    } catch (IOException e) {
                        e.printStackTrace();
                        startPollingWorker(nodeId);
                    }

                    //get response as String or what ever way you need

                }
            }, 0, 30000);
*/



        //recursive
       /*
        Thread pollThead =   new Thread() {
            public void run() {

                try {
                    logger.debug("Polling nodeId {}", nodeId);
                    JsonObject httpResponse = HttpHelper.httpPoll(HttpHelper.getConnection(pollingEndpoint + "?nodeId=" + nodeId), "{}");
                    logger.debug("Polling for nodeId {} receive a httpResonse {}", nodeId, httpResponse);
                    processHttpResponseDTO(httpResponse);
                    startPollingWorker(nodeId);
                } catch (IOException e) {
                    logger.debug("Polling for nodeId {} didn't receive any response, send it again ");
                    //e.printStackTrace();
                    startPollingWorker(nodeId);
                }
            }

            ;
        };

        */
        Thread pollThead =   new Thread() {
            public void run() {
                //Boolean exit = false;
                while(true){
                    try {
                        logger.trace("Polling nodeId {}", nodeId);
                        JsonObject httpResponse = HttpHelper.httpPoll(HttpHelper.getConnection(pollingEndpoint + "?nodeId=" + nodeId), "{}");
                        logger.trace("Polling for nodeId {} receive a httpResonse {}", nodeId, httpResponse);
                        processHttpResponseDTO(httpResponse);
                    } catch (IOException e) {
                        logger.trace("Polling for nodeId {} didn't receive any response, send it again ");
                        //e.printStackTrace();
                    }
                }
            }
        };

        pollThead.start();

    }


    private void processHttpResponseDTO(JsonObject httpResponse){

        // is response DTO ?
        if (KeypleDTOHelper.isKeypleDTO(httpResponse)) {

            KeypleDTO responseDTO = KeypleDTOHelper.fromJsonObject(httpResponse);
            TransportDTO transportDTO = new WsPTransportDTO(responseDTO, null);
            // connection
            TransportDTO sendback = this.dtoDispatcher.onDTO(transportDTO);

            // if sendBack is not a not reponse
            if (!KeypleDTOHelper.isNoResponse(sendback.getKeypleDTO())) {
                this.sendDTO(sendback);
            }
        }

    }


    @Override
    public void sendDTO(TransportDTO tdto) {
        KeypleDTO ktdo = tdto.getKeypleDTO();
        logger.debug("Ws Slave send DTO {}", KeypleDTOHelper.toJson(ktdo));
        if (!KeypleDTOHelper.isNoResponse(tdto.getKeypleDTO())) {
            try {
                // send keyple dto
                JsonObject httpResponse = HttpHelper.httpPOSTJson(HttpHelper.getConnection(endoint),
                        KeypleDTOHelper.toJson(ktdo));

                processHttpResponseDTO(httpResponse);

            } catch (IOException e) {
                e.printStackTrace();
                // todo manage exception or throw it
            }
        }
    }

    @Override
    public void sendDTO(KeypleDTO message) {
        sendDTO(new WsTransportDTO(message, null));
    }

    @Override
    public void update(KeypleDTO event) {
        this.sendDTO(event);
    }


    /*
     * TransportNode
     */
    @Override
    public void setDtoDispatcher(DtoDispatcher dtoDispatcher) {
        this.dtoDispatcher = dtoDispatcher;
    }


}
