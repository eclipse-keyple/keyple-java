package org.eclise.keyple.example.remote.server;

import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclise.keyple.example.remote.server.transport.ClientTransport;
import org.eclise.keyple.example.remote.server.transport.ServerTransport;

import java.io.IOException;
import java.util.Map;


public class RemoteSeReader implements ProxyReader{

    ServerTransport transport;

    public RemoteSeReader(ServerTransport _transport){
        transport = _transport;
    }


    @Override
    public String getName() {
        return String.valueOf(transport.hashCode());
    }

    @Override
    public boolean isSePresent() throws NoStackTraceThrowable {
        return transport.isSePresent();
    }

    @Override
    public SeResponseSet transmit(SeRequestSet seApplicationRequest) throws IOReaderException {
        return transport.transmit(seApplicationRequest);
    }

    @Override
    public void addSeProtocolSetting(SeProtocolSetting seProtocolSetting) {
        transport.addSeProtocolSetting(seProtocolSetting);
    }

    @Override
    public int compareTo(ProxyReader o) {
        return this.getName().equals(String.valueOf(((RemoteSeReader)o).hashCode()))? 1 : 0);
    }

    @Override
    public Map<String, String> getParameters() {
        return null;
    }

    @Override
    public void setParameter(String key, String value) throws IOException { }

    @Override
    public void setParameters(Map<String, String> parameters) throws IOException {}
}
