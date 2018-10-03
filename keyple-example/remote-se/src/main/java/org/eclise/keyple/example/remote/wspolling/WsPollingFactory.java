package org.eclise.keyple.example.remote.wspolling;

import org.eclipse.keyple.plugin.remote_se.transport.ServerNode;
import org.eclipse.keyple.plugin.remote_se.transport.TransportNode;
import org.eclise.keyple.example.remote.common.TransportFactory;

import java.io.IOException;

public class WsPollingFactory extends TransportFactory {

    Boolean localhost = true;
    Integer port = 8007;
    String pollingUrl = "/polling";
    String keypleUrl = "/keypleDTO";
    String nodeId = "local1";
    String bindUrl="0.0.0.0";
    String protocol = "http://";


    @Override
    public TransportNode getClient(Boolean isMaster) {

        WsPClient client = new WsPClient(protocol+ "localhost:"+port+keypleUrl, protocol+ "localhost:"+port+pollingUrl);
        client.startPollingWorker(nodeId);
        return client;
    }

    @Override
    public ServerNode getServer(Boolean isMaster) throws IOException {
        if(localhost){
            bindUrl = "0.0.0.0";
        }

        return new WsPServer(bindUrl, port, keypleUrl,pollingUrl);

    }
}
