package org.eclipse.keyple.example.remote.websocket;

import org.eclipse.keyple.example.remote.common.TransportFactory;
import org.eclipse.keyple.plugin.remote_se.transport.ClientNode;
import org.eclipse.keyple.plugin.remote_se.transport.ServerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

public class WskFactory extends TransportFactory {

    Boolean localhost = true;
    Integer port = 8002;
    String pollingUrl = "/polling";
    String keypleUrl = "/keypleDTO";
    String nodeId = "local1";
    String bindUrl="0.0.0.0";
    String protocol = "http://";

    private static final Logger logger = LoggerFactory.getLogger(WskFactory.class);


    @Override
    public ClientNode getClient(Boolean isMaster) {

        logger.info("*** Create Websocket Client ***");


        ClientNode wskClient = null;
        try {
            wskClient = new WskClient(new URI(protocol+ "localhost:"+port+keypleUrl));
            //wskClient.connect();
            return  wskClient;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public ServerNode getServer(Boolean isMaster) throws IOException {

        logger.info("*** Create Websocket Server ***");

        InetSocketAddress inet = new InetSocketAddress(Inet4Address.getByName(bindUrl), port);
        WskServer wskServer = new WskServer(inet, null, !isMaster);
        return wskServer;
    }
}
