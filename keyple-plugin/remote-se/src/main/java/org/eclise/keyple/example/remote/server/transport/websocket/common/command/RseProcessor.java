package org.eclise.keyple.example.remote.server.transport.websocket.common.command;

import org.eclise.keyple.example.remote.server.transport.NseClient;
import org.eclise.keyple.example.remote.server.transport.RseAPI;
import org.eclise.keyple.example.remote.server.transport.websocket.common.KeypleCommand;
import org.eclise.keyple.example.remote.server.transport.websocket.common.KeypleDTO;
import org.eclise.keyple.example.remote.server.transport.websocket.common.KeypleDTOHelper;
import org.eclise.keyple.example.remote.server.transport.websocket.common.command.rse.OnReaderConnect;
import org.eclise.keyple.example.remote.server.transport.websocket.common.command.rse.OnReaderDisconnect;
import org.eclise.keyple.example.remote.server.transport.websocket.common.command.rse.OnReaderEvent;

/**
 * Remote RseProcessor  Process :
 * CONNECT
 * READER_EVENT
 * DISCONNECT
 *
 * Receive response :
 * SeResponseSet
 */
public class RseProcessor {

    RseAPI rseAPI;
    NseClient nseClient;

    public RseProcessor(RseAPI rseAPI, NseClient nseClient) {
        this.rseAPI = rseAPI;
        this.nseClient = nseClient;
    }

    public KeypleDTO processMessage(KeypleDTO msg){

        //todo verify integrity
        if(!KeypleDTOHelper.verifyHash(msg, msg.getHash())){
            //return exception
        }

        if(msg.getAction().equals(KeypleDTOHelper.READER_EVENT)){
            KeypleCommand command = new OnReaderEvent(rseAPI);
            return command.process(msg);
        }

        if(msg.getAction().equals(KeypleDTOHelper.READER_CONNECT)){
            KeypleCommand command = new OnReaderConnect(rseAPI);
            return command.process(msg);
        }

        if(msg.getAction().equals(KeypleDTOHelper.READER_DISCONNECT)){
            KeypleCommand command = new OnReaderDisconnect(rseAPI);
            return command.process(msg);
        }

        if(msg.getAction().equals(KeypleDTOHelper.READER_TRANSMIT)){
            KeypleCommand command = new OnReaderDisconnect(rseAPI);
            return command.process(msg);
        }

        return null;
    }

}
