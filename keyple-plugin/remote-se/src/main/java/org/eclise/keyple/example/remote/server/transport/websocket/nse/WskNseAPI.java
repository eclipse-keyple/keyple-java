package org.eclise.keyple.example.remote.server.transport.websocket.nse;

import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclise.keyple.example.remote.server.transport.NseAPI;

public class WskNseAPI implements NseAPI {
    @Override
    public SeResponseSet onTransmit(SeRequestSet req) {
        return null;
    }

    @Override
    public String onGetName() {
        return null;
    }

    @Override
    public Boolean onIsSePresent() {
        return null;
    }

    @Override
    public void onAddSeProtocolSetting(SeProtocolSetting seProtocolSetting) {

    }
}
