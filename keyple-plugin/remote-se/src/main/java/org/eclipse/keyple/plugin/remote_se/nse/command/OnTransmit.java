package org.eclipse.keyple.plugin.remote_se.nse.command;

import org.eclipse.keyple.plugin.remote_se.nse.KeypleCommand;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.plugin.remote_se.transport.json.SeProxyJsonParser;

import org.eclipse.keyple.plugin.remote_se.nse.NseAPI;
import org.eclipse.keyple.plugin.remote_se.transport.KeypleDTO;


public class OnTransmit extends KeypleCommand {

    private NseAPI nseAPI;

    public OnTransmit(NseAPI nseAPI){
        this.nseAPI = nseAPI;
    }

    public KeypleDTO process(KeypleDTO keypleDTO) {
        SeRequestSet seRequestSet = SeProxyJsonParser.getGson().fromJson(keypleDTO.getBody(), SeRequestSet.class);
        SeResponseSet seResponseSet = null;
        try {
            seResponseSet = nseAPI.onTransmit(seRequestSet);
        } catch (IOReaderException e) {
            e.printStackTrace();
        }
        String parseBody = SeProxyJsonParser.getGson().toJson(seResponseSet, SeResponseSet.class);
        return new KeypleDTO(keypleDTO.getSessionId(), keypleDTO.getAction(),  parseBody, false);
    }
}