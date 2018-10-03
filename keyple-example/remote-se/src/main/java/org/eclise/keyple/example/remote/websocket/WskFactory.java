package org.eclise.keyple.example.remote.websocket;

import org.eclipse.keyple.plugin.remote_se.transport.ClientNode;
import org.eclipse.keyple.plugin.remote_se.transport.ServerNode;
import org.eclipse.keyple.plugin.remote_se.transport.TransportNode;
import org.eclise.keyple.example.remote.common.TransportFactory;
import org.eclise.keyple.example.remote.wspolling.WsPClient;
import org.eclise.keyple.example.remote.wspolling.WsPServer;
import org.java_websocket.client.WebSocketClient;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

public class WskFactory extends TransportFactory {

    Boolean localhost = true;
    Integer port = 8007;
    String pollingUrl = "/polling";
    String keypleUrl = "/keypleDTO";
    String nodeId = "local1";
    String bindUrl="0.0.0.0";
    String protocol = "http://";


    @Override
    public TransportNode getClient(Boolean isMaster) {

        ClientNode wskClient = null;
        try {
            wskClient = new WskClient(new URI(protocol+ "localhost:"+port+keypleUrl));
            wskClient.connect();
            return  wskClient;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public ServerNode getServer(Boolean isMaster) throws IOException {
        InetSocketAddress inet = new InetSocketAddress(Inet4Address.getByName(bindUrl), port);
        WskServer wskServer = new WskServer(inet, null, !isMaster);
        return wskServer;
    }
}
