package org.eclise.keyple.example.remote.server;

import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;

import java.io.IOException;
import java.util.Map;


public class RemoteSeReader implements ProxyReader, RemoteReaderListener {


    ProxyReader _remoteReader;

    public RemoteSeReader(ProxyReader remoteReader){
        _remoteReader = remoteReader;


    }


    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean isSePresent() throws NoStackTraceThrowable {
        return false;
    }

    @Override
    public SeResponseSet transmit(SeRequestSet seApplicationRequest) throws IOReaderException {
        return null;
    }

    @Override
    public void addSeProtocolSetting(SeProtocolSetting seProtocolSetting) {

    }

    @Override
    public int compareTo(ProxyReader o) {
        return 0;
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
    public void onRemoteReaderEvent() {

    }
}
