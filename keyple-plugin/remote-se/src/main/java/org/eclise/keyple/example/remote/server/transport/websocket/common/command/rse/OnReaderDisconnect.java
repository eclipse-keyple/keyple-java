package org.eclise.keyple.example.remote.server.transport.websocket.common.command.rse;

import com.google.gson.JsonObject;
import org.eclise.keyple.example.remote.server.serializer.json.SeProxyJsonParser;
import org.eclise.keyple.example.remote.server.transport.RseAPI;
import org.eclise.keyple.example.remote.server.transport.websocket.common.KeypleCommand;
import org.eclise.keyple.example.remote.server.transport.websocket.common.KeypleDTO;

public class OnReaderDisconnect extends KeypleCommand {

    public RseAPI rseAPI;

    public OnReaderDisconnect(RseAPI rseAPI){
        this.rseAPI = rseAPI;
    }

    @Override
    public KeypleDTO process(KeypleDTO keypleDTO) {
        JsonObject body = SeProxyJsonParser.getGson().fromJson(keypleDTO.getBody(), JsonObject.class);
        String readerName = body.get("localReaderName").getAsString();
        String sessionId = rseAPI.onReaderDisconnect(readerName);
        return new KeypleDTO(sessionId, keypleDTO.getAction(), "",false);
    }
}
