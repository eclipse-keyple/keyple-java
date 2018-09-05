package org.eclise.keyple.example.remote.server.transport.local;

import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclise.keyple.example.remote.server.transport.ClientListener;
import org.eclise.keyple.example.remote.server.transport.ClientTransport;
import org.eclise.keyple.example.remote.server.transport.ServerListener;
import org.eclise.keyple.example.remote.server.transport.ServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class LocalServerTransport implements ServerTransport {

    private static final Logger logger = LoggerFactory.getLogger(LocalServerTransport.class);

    ClientListener client;

    public LocalServerTransport(){
        logger.info("Constructor empty");
    }

    public void setClientListener(ClientListener _client){
        logger.info("Constructor with client listener {}", client);
        client = _client;
    }

    public ClientListener getClientListener(){
        return client;
    }


    @Override
    public String getName() {
        logger.debug("getName");
        return client.onGetName();
    }

    @Override
    public boolean isSePresent() throws NoStackTraceThrowable {
        logger.debug("isSePresent");
        return client.onIsSePresent();
    }

    @Override
    public SeResponseSet transmit(SeRequestSet seApplicationRequest) throws IOReaderException {
        logger.debug("transmit {}", seApplicationRequest);
        return client.onTransmit(seApplicationRequest);
    }

    @Override
    public void addSeProtocolSetting(SeProtocolSetting seProtocolSetting) {
        logger.debug("addSeProtocolSetting {}", seProtocolSetting);
        client.onAddSeProtocolSetting(seProtocolSetting);
    }









    @Override
    public Map<String, String> getParameters() {
        return null;
    }

    @Override
    public void setParameter(String key, String value) throws IOException {

    }

    @Override
    public void setParameters(Map<String, String> parameters) throws IOException {

    }

    @Override
    public int compareTo(ProxyReader o) {
        return 0;
    }
}
