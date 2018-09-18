package org.eclise.keyple.example.remote.server.transport.websocket.common.command.rse;

import com.google.gson.JsonObject;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclise.keyple.example.remote.server.serializer.json.SeProxyJsonParser;
import org.eclise.keyple.example.remote.server.transport.NseClient;
import org.eclise.keyple.example.remote.server.transport.RseAPI;
import org.eclise.keyple.example.remote.server.transport.websocket.common.KeypleCommand;
import org.eclise.keyple.example.remote.server.transport.websocket.common.KeypleDTO;

public class OnTransmitResponse extends KeypleCommand {

    public NseClient nseClient;

    public OnTransmitResponse(NseClient nseClient){
        this.nseClient = nseClient;
    }

    @Override
    public KeypleDTO process(KeypleDTO keypleDTO) {
        SeResponseSet seResponseSet = SeProxyJsonParser.getGson().fromJson(keypleDTO.getBody(), SeResponseSet.class);

        //todo

        return null;
    }
}
