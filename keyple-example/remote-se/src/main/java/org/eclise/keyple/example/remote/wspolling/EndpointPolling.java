package org.eclise.keyple.example.remote.wspolling;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.eclipse.keyple.plugin.remote_se.transport.*;
import org.eclise.keyple.example.remote.ws.HttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class EndpointPolling implements HttpHandler, TransportNode {


        private final Logger logger = LoggerFactory.getLogger(EndpointPolling.class);

        DtoDispatcher dtoDispatcher;
        private Queue<HttpExchange> requestQueue;

        public EndpointPolling(Queue requestQueue){
            this.requestQueue = requestQueue;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {

            logger.trace("Incoming HttpExchange {} ", t.toString());
            logger.trace("Incoming Request {} ", t.getRequestMethod());
            String requestMethod = t.getRequestMethod();

            if (requestMethod.equals("POST")) {

                //hold response until we got a response or timeout
                Map<String, String> params = queryToMap(t.getRequestURI().getQuery());
                String nodeId = params.get("nodeId");
                //logger.trace("param nodeId=" + params.get("nodeId"));

                logger.trace("Receive a polling request {} for nodeId {}, add it to the queue, queue size before adding {}", t.toString(),nodeId, requestQueue.size());

                //set httpExchange in queue
                requestQueue.add(t);

                //setHttpResponse(t, KeypleDTOHelper.ACK());

            }
        }

        /*
         * TransportNode
         */
        @Override
        public void setDtoDispatcher(DtoDispatcher receiver) {
            this.dtoDispatcher = receiver;
        }


        @Override
        public void sendDTO(TransportDTO message) {
            logger.warn("Send DTO with transportmessage {}", message);
            this.sendDTO(message.getKeypleDTO());
        }

        @Override
        public void sendDTO(KeypleDTO message) {
            logger.info("Using polling to send keypleDTO {}", KeypleDTOHelper.toJson(message));

            synchronized (requestQueue){
                logger.debug("Polling Queue size {}", requestQueue.size());

                if(requestQueue.size() == 0){
                    logger.warn("Too bad request Queue is empty, impossible to send DTO");
                }else{

                    HttpExchange t = requestQueue.poll();

                    try {
                        logger.debug("Found a wainting HttpExchange {}", t.toString());
                        HttpHelper.setHttpResponse(t, message);
                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.error("Response to polling has failed");
                    }
                }
            }
        }




        @Override
        public void update(KeypleDTO event) {
            logger.info("Send DTO from update {}", event);
            this.sendDTO(event);
        }


    private Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<String, String>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            }else{
                result.put(entry[0], "");
            }
        }
        return result;
    }
    }

