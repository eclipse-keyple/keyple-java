package org.eclise.keyple.example.remote.server.transport.websocket.common.command.nse;

import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclise.keyple.example.remote.server.serializer.json.SeProxyJsonParser;
import org.eclise.keyple.example.remote.server.transport.NseAPI;
import org.eclise.keyple.example.remote.server.transport.websocket.common.KeypleDTO;
import org.eclise.keyple.example.remote.server.transport.websocket.common.KeypleCommand;

public class OnTransmit extends KeypleCommand {

    private NseAPI nseAPI;

    public OnTransmit(NseAPI nseAPI){
        this.nseAPI = nseAPI;
    }

    public KeypleDTO process(KeypleDTO keypleDTO) {
        SeRequestSet seRequestSet = SeProxyJsonParser.getGson().fromJson(keypleDTO.getBody(), SeRequestSet.class);
        SeResponseSet seResponseSet = nseAPI.onTransmit(seRequestSet);
        String parseBody = SeProxyJsonParser.getGson().toJson(seResponseSet, SeResponseSet.class);
        return new KeypleDTO(keypleDTO.getSessionId(), keypleDTO.getAction(),  parseBody, false);
    }
}
