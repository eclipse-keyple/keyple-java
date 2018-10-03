package org.eclise.keyple.example.remote.common;

import org.eclipse.keyple.plugin.remote_se.transport.ServerNode;
import org.eclipse.keyple.plugin.remote_se.transport.TransportNode;
import org.eclise.keyple.example.remote.wspolling.WsPClient;

import java.io.IOException;

public abstract class TransportFactory {

    abstract public TransportNode getClient(Boolean isMaster);
    abstract public ServerNode getServer(Boolean isMaster) throws IOException;


}
