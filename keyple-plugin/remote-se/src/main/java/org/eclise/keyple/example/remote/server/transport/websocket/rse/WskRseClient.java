package org.eclise.keyple.example.remote.server.transport.websocket.rse;

import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclise.keyple.example.remote.server.transport.RseClient;
import org.eclise.keyple.example.remote.server.transport.RseNseSession;

import java.io.IOException;
import java.util.Map;

public class WskRseClient implements RseClient{

    @Override
    public String connectReader(ProxyReader localReader, Map<String, Object> options) throws IOException {
        return null;
    }

    @Override
    public String disconnectReader(ProxyReader localReader) throws IOException {
        return null;
    }

    @Override
    public void update(ReaderEvent event) {

    }
}
