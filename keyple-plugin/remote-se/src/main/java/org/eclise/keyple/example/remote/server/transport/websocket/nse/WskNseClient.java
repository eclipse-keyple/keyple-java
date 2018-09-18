package org.eclise.keyple.example.remote.server.transport.websocket.nse;

import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclise.keyple.example.remote.server.transport.NseClient;

public class WskNseClient implements NseClient {

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean isSePresent() {
        return false;
    }

    @Override
    public void addSeProtocolSetting(SeProtocolSetting seProtocolSetting) {

    }

    @Override
    public SeResponseSet transmit(SeRequestSet seApplicationRequest) {
        return null;
    }

    @Override
    public String getSessionId() {
        return null;
    }

    @Override
    public Boolean isDuplex() {
        return null;
    }
}
