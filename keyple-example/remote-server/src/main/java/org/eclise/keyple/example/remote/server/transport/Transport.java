package org.eclise.keyple.example.remote.server.transport;

import java.util.Properties;

public interface Transport {

    public Boolean configure(Properties configuration);

    public Boolean start(TransportListener listener);


}

