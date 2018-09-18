package org.eclise.keyple.example.remote.server.transport.websocket.common.command.rse;

import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclise.keyple.example.remote.server.serializer.json.SeProxyJsonParser;
import org.eclise.keyple.example.remote.server.transport.RseAPI;
import org.eclise.keyple.example.remote.server.transport.websocket.common.KeypleDTO;
import org.eclise.keyple.example.remote.server.transport.websocket.common.KeypleDTOHelper;
import org.eclise.keyple.example.remote.server.transport.websocket.common.KeypleCommand;

public class OnReaderEvent extends KeypleCommand {

    public RseAPI rseAPI;

    public OnReaderEvent(RseAPI rseAPI){
        this.rseAPI = rseAPI;
    }

    @Override
    public KeypleDTO process(KeypleDTO keypleDTO) {
        ReaderEvent event = SeProxyJsonParser.getGson().fromJson(keypleDTO.getBody(), ReaderEvent.class);
        rseAPI.onRemoteReaderEvent(event);
        return KeypleDTOHelper.getEmptyDto();
    }
}
