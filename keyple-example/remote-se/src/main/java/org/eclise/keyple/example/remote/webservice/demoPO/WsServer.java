package org.eclise.keyple.example.remote.webservice.demoPO;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.eclipse.keyple.plugin.remote_se.transport.*;
import org.eclise.keyple.example.remote.webservice.common.HttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;

public class WsServer implements TransportNode{

    private InetSocketAddress inet;
    private String endpoint;
    private HttpServer server;
    static private Integer MAX_CONNECTION = 5;
    private KeypleDTOEndpoint keypleDTOEndpoint;

    private static final Logger logger = LoggerFactory.getLogger(WsServer.class);


    public WsServer(String url, Integer port, String endpoint) throws IOException {
        logger.info("Init Web Service Server on url : {}:{}", url,port);

        // Create Endpoints for plugin and reader API
        keypleDTOEndpoint = new KeypleDTOEndpoint();
        //ReaderEndpoint readerEndpoint = new ReaderEndpoint();

        // deploy endpoint
        this.inet = new InetSocketAddress(Inet4Address.getByName(url), port);
        this.endpoint = endpoint;
        server = HttpServer.create(inet, MAX_CONNECTION);
        server.createContext(endpoint, keypleDTOEndpoint);

        // start rse
        server.setExecutor(null); // creates a default executor
    }

    public void start(){
        logger.info("Starting Server on http://{}:{}{}", inet.getHostName(), inet.getPort(),
                endpoint);
        server.start();
    }

    /*
    TransportNode
     */
    @Override
    public void setDtoReceiver(DtoReceiver receiver) {
        this.keypleDTOEndpoint.setDtoReceiver(receiver);;
    }

    @Override
    public Object getConnection(String sessionId) {
        return this.keypleDTOEndpoint.getConnection(sessionId);
    }

    @Override
    public void sendDTO(TransportDTO message) {
        this.keypleDTOEndpoint.sendDTO(message);
    }

    @Override
    public void sendDTO(KeypleDTO message) {

    }


    private class KeypleDTOEndpoint implements HttpHandler, TransportNode {

        private final Logger logger = LoggerFactory.getLogger(KeypleDTOEndpoint.class);

        DtoReceiver dtoReceiver;

        @Override
        public void handle(HttpExchange t) throws IOException {

            logger.trace("Incoming Request {} ", t.getRequestMethod());
            String requestMethod = t.getRequestMethod();

            if (requestMethod.equals("POST")) {
                String body = HttpHelper.parseBodyToString(t.getRequestBody());// .. parse the request body
                KeypleDTO incoming = KeypleDTOHelper.fromJson(body);
                TransportDTO transportDTO = new WsTransportDTO(incoming,t);

                logger.trace("Incoming DTO {} ", KeypleDTOHelper.toJson(incoming));
                TransportDTO outcoming = dtoReceiver.onDTO(transportDTO);

                setHttpResponse(t,outcoming.getKeypleDTO());

            }
        }

        @Override
        public void setDtoReceiver(DtoReceiver receiver) {
            this.dtoReceiver = receiver;
        }

        @Override
        public Object getConnection(String sessionId) {
            return null;//stateless not in use;
        }

        @Override
        public void sendDTO(TransportDTO message) {
            logger.warn("Send DTO can not be used in Web Service Server");
            //not in use, oneway communication, server do not send message
        }

        @Override
        public void sendDTO(KeypleDTO message) {
            logger.warn("Send DTO can not be used in Web Service Server");
        }


        private void setHttpResponse(HttpExchange t, KeypleDTO resp) throws IOException {
            if(!resp.getAction().isEmpty()){
                String responseBody = KeypleDTOHelper.toJson(resp);
                Integer responseCode = 200;
                t.getResponseHeaders().add("Content-Type", "application/json");
                t.sendResponseHeaders(responseCode, responseBody.length());
                OutputStream os = t.getResponseBody();
                os.write(responseBody.getBytes());
                os.close();
                logger.debug("Outcoming Response Code {} ", responseCode);
                logger.debug("Outcoming Response Body {} ", responseBody);
            }else{
                String responseBody = "{}";
                Integer responseCode = 200;
                t.getResponseHeaders().add("Content-Type", "application/json");
                t.sendResponseHeaders(responseCode, responseBody.length());
                OutputStream os = t.getResponseBody();
                os.write(responseBody.getBytes());
                os.close();
            }
        }
    }

}
