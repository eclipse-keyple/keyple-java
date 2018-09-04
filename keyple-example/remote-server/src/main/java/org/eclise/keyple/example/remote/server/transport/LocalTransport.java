package org.eclise.keyple.example.remote.server.transport;

import org.eclipse.keyple.seproxy.ProxyReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class LocalTransport implements Transport {

    private static final Logger logger = LoggerFactory.getLogger(LocalTransport.class);

    TransportListener serverlistener;

    public LocalTransport(){
        logger.info("LocalTransport constructor");
    }

    @Override
    public Boolean configure(Properties configuration) {
        logger.info("configure {}", configuration);
        return true;
    }

    @Override
    public Boolean start(TransportListener listener) {
        logger.info("start {}", listener);
        serverlistener = listener;
        return true;
    }

    public Boolean clientConnect(ProxyReader reader){
        logger.info("clientConnect {}", reader);
        serverlistener.onReaderConnected(reader);
        return true;
    }

}
