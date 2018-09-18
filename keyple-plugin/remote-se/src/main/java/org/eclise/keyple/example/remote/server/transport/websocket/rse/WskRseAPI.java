package org.eclise.keyple.example.remote.server.transport.websocket.rse;

import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclise.keyple.example.remote.server.transport.RseAPI;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.util.Map;

public class WskRseAPI implements RseAPI {

    @Override
    public String onReaderConnect(String readerName, Map<String,Object > options) {
        return null;
    }

    @Override
    public String onReaderDisconnect(String readerName) {
        return null;
    }

    @Override
    public void onRemoteReaderEvent(ReaderEvent event) {

    }


}
