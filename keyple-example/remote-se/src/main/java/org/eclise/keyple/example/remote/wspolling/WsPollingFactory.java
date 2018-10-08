package org.eclise.keyple.example.remote.wspolling;

import org.eclipse.keyple.plugin.remote_se.transport.ClientNode;
import org.eclipse.keyple.plugin.remote_se.transport.ServerNode;
import org.eclipse.keyple.plugin.remote_se.transport.TransportNode;
import org.eclise.keyple.example.remote.common.TransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class WsPollingFactory extends TransportFactory {

    Boolean localhost = true;
    Integer port = 8002;
    String pollingUrl = "/polling";
    String keypleUrl = "/keypleDTO";
    String nodeId = "local1";
    String bindUrl="0.0.0.0";
    String protocol = "http://";

    private static final Logger logger = LoggerFactory.getLogger(WsPollingFactory.class);


    @Override
    public ClientNode getClient(Boolean isMaster) {

        logger.info("*** Create Ws Polling Client ***");

        WsPClient client = new WsPClient(protocol+ "localhost:"+port+keypleUrl, protocol+ "localhost:"+port+pollingUrl, nodeId);
        //client.startPollingWorker(nodeId);
        return client;
    }

    @Override
    public ServerNode getServer(Boolean isMaster) throws IOException {
        if(localhost){
            bindUrl = "0.0.0.0";
        }

        logger.info("*** Create Ws Polling Server ***");

        return new WsPServer(bindUrl, port, keypleUrl,pollingUrl);

    }
}
