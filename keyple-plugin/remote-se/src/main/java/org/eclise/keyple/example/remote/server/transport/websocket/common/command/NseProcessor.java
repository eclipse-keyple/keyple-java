package org.eclise.keyple.example.remote.server.transport.websocket.common.command;

import org.eclise.keyple.example.remote.server.transport.NseAPI;
import org.eclise.keyple.example.remote.server.transport.RseClient;
import org.eclise.keyple.example.remote.server.transport.websocket.common.KeypleCommand;
import org.eclise.keyple.example.remote.server.transport.websocket.common.KeypleDTO;
import org.eclise.keyple.example.remote.server.transport.websocket.common.KeypleDTOHelper;
import org.eclise.keyple.example.remote.server.transport.websocket.common.command.nse.OnTransmit;

/**
 * Native Processor processes :
 * TRANSMIT
 *
 * Receives result :
 * READER_CONNECT
 * READER_DISCONNECT
 */
public class NseProcessor {

    NseAPI nseAPI;
    RseClient rseClient;

    public NseProcessor(NseAPI nseAPI, RseClient rseClient) {
        this.nseAPI = nseAPI;
        this.rseClient = rseClient;
    }


    public KeypleDTO processMessage(KeypleDTO msg){

        //todo verify integrity
        if(!KeypleDTOHelper.verifyHash(msg, msg.getHash())){
            //return exception
        }

        if(msg.getAction().equals(KeypleDTOHelper.READER_TRANSMIT) ){
            KeypleCommand command = new OnTransmit(nseAPI);
            return command.process(msg);
        }

        return null ;

    }

}
