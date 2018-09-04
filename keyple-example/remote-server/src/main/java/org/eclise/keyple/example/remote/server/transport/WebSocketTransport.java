package org.eclise.keyple.example.remote.server.transport;

import org.eclise.keyple.example.remote.server.transport.Transport;
import org.eclise.keyple.example.remote.server.transport.TransportListener;

import java.util.Properties;

public class WebSocketTransport implements Transport {

    @Override
    public Boolean configure(Properties configuration) {
        return null;
    }

    @Override
    public Boolean start(TransportListener listener) {
        return null;
    }


}
