package org.eclise.keyple.example.remote.server.transport;

import java.util.Properties;

public class TransportFactory {

    static Transport transport = new LocalTransport();

    static public Transport getTransport(Properties configuration){
        if(configuration == null){
            return transport;
        }
        return null;
    }

}
