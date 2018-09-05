package org.eclise.keyple.example.remote.server.transport.local;

import org.eclise.keyple.example.remote.server.transport.*;
import org.eclise.keyple.example.remote.server.transport.local.LocalServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.MissingResourceException;
import java.util.Properties;

public class LocalTransportFactory implements TransportFactory {

    private static final Logger logger = LoggerFactory.getLogger(LocalTransportFactory.class);

    static LocalTransportFactory uniqueInstance = new LocalTransportFactory();

    //ServerTransport must be initialized before the ClientTransport
    ServerTransport serverTransport;
    ServerListener serverListener;

    ClientTransport clientTransport;

    private LocalTransportFactory(){
        //public contrusction singleton
    }

    public static LocalTransportFactory getInstance(){
        return uniqueInstance;
    }


    @Override
    public ServerListener initServerListener() {
        if(serverListener == null){
            serverListener = new LocalServerListener();
            serverTransport = new LocalServerTransport(); // prepare transport for client
        }
        return serverListener;
    }

    public ServerTransport getServerTransport() {
        return serverTransport;
    }

    @Override
    public ClientTransport getClientTransport(ClientListener clientlistener) throws UnknownHostException{
        logger.debug("getClientTransport {}", clientlistener);
        if (serverListener == null) {
            logger.error("Server has not been initialized");
            throw new UnknownHostException("Server has not been initialized");
        } else {
            if (clientTransport == null) {
                logger.info("Init duplex connection");
                //link transport with listener in duplex (client-server and server-client)
                ((LocalServerTransport) serverTransport).setClientListener(clientlistener);
                clientTransport = new LocalClientTransport(serverListener);
            } else {
                logger.debug("Duplex connection already created");
            }
            logger.debug("returning client transport {}", clientTransport);
            return clientTransport;
        }
    }


}
