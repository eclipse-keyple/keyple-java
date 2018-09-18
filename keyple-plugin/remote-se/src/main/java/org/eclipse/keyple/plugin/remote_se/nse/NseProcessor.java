package org.eclipse.keyple.plugin.remote_se.nse;

import org.eclipse.keyple.plugin.remote_se.nse.command.OnTransmit;
import org.eclipse.keyple.plugin.remote_se.transport.KeypleDTO;
import org.eclipse.keyple.plugin.remote_se.transport.KeypleDTOHelper;

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
