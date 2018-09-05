package org.eclise.keyple.example.remote.server.transport;

import java.net.UnknownHostException;

public interface TransportFactory {


    public ClientTransport getClientTransport(ClientListener clientListener) throws UnknownHostException;

    public ServerListener initServerListener();
}
