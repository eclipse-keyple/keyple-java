package org.eclise.keyple.example.remote.wspolling;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.eclipse.keyple.plugin.remote_se.transport.*;
import org.eclise.keyple.example.remote.ws.HttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public class EndpointKeypleDTO implements HttpHandler, TransportNode {


    private final Logger logger = LoggerFactory.getLogger(EndpointKeypleDTO.class);

    DtoDispatcher dtoDispatcher;
    DtoSender dtoSender;

    public EndpointKeypleDTO(DtoSender dtoSender) {
        this.dtoSender = dtoSender;//endpointPolling
    }

    @Override
        public void handle(HttpExchange t) throws IOException {

            logger.trace("Incoming HttpExchange {} ", t.toString());
            logger.trace("Incoming Request {} ", t.getRequestMethod());
            String requestMethod = t.getRequestMethod();

            if (requestMethod.equals("POST")) {
                String body = HttpHelper.parseBodyToString(t.getRequestBody());// .. parse the
                // request body
                KeypleDTO incoming = KeypleDTOHelper.fromJson(body);
                TransportDTO transportDTO = new WsPTransportDTO(incoming, dtoSender);

                logger.trace("Incoming DTO {} ", KeypleDTOHelper.toJson(incoming));
                TransportDTO outcoming = dtoDispatcher.onDTO(transportDTO);

                setHttpResponse(t, outcoming.getKeypleDTO());

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
            logger.warn("Send DTO can not be used in Web Service Master");
            // not in use, oneway communication, server do not send message
        }

        @Override
        public void sendDTO(KeypleDTO message) {
            logger.warn("Send DTO can not be used in Web Service Master");
        }


        private void setHttpResponse(HttpExchange t, KeypleDTO resp) throws IOException {
            if (!resp.getAction().isEmpty()) {
                String responseBody = KeypleDTOHelper.toJson(resp);
                Integer responseCode = 200;
                t.getResponseHeaders().add("Content-Type", "application/json");
                t.sendResponseHeaders(responseCode, responseBody.length());
                OutputStream os = t.getResponseBody();
                os.write(responseBody.getBytes());
                os.close();
                logger.debug("Outcoming Response Code {} ", responseCode);
                logger.debug("Outcoming Response Body {} ", responseBody);
            } else {
                String responseBody = "{}";
                Integer responseCode = 200;
                t.getResponseHeaders().add("Content-Type", "application/json");
                t.sendResponseHeaders(responseCode, responseBody.length());
                OutputStream os = t.getResponseBody();
                os.write(responseBody.getBytes());
                os.close();
            }
        }

        @Override
        public void update(KeypleDTO event) {
            // not in used in ws
        }
    }

