package org.eclipse.keyple.example.remote.common;

import org.eclipse.keyple.plugin.remote_se.transport.ClientNode;
import org.eclipse.keyple.plugin.remote_se.transport.ServerNode;

import java.io.IOException;

public abstract class TransportFactory {

    abstract public ClientNode getClient(Boolean isMaster);
    abstract public ServerNode getServer(Boolean isMaster) throws IOException;


}
