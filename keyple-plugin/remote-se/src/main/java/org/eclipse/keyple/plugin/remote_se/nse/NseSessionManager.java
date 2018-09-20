package org.eclipse.keyple.plugin.remote_se.nse;

import org.eclipse.keyple.seproxy.exception.UnexpectedReaderException;

import java.util.HashMap;
import java.util.Map;

public class NseSessionManager {



    private Map<String, String> readerName_sessionId;

    public NseSessionManager(){
        readerName_sessionId = new HashMap<String, String>();
    }


    void addNewSession(String sessionId, String readerName){
        readerName_sessionId.put(readerName, sessionId);

    }


    String getLastSession(String readerName){
        return readerName_sessionId.get(readerName);
    }

    String findReaderNameBySession(String sessionId) throws UnexpectedReaderException{
        for(String readerName : readerName_sessionId.keySet()){
            if(readerName_sessionId.get(readerName).equals(sessionId)){
                return readerName;
            }
        }
        throw new UnexpectedReaderException("Reader not found by sessionId");
    }

}
