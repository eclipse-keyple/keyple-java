package org.eclise.keyple.example.remote.server.transport.websocket.common;

import org.eclise.keyple.example.remote.server.serializer.json.SeProxyJsonParser;

public class KeypleDTOHelper {

    public static String READER_TRANSMIT = "reader_transmit";
    public static String READER_CONNECT = "reader_connect";
    public static String READER_DISCONNECT = "reader_disconnect";
    public static String READER_EVENT = "reader_event";

    static public int generateHash(KeypleDTO keypleDTO){
        return keypleDTO.hashCode();
    }

    static public Boolean verifyHash(KeypleDTO keypleDTO, int hash){
        return keypleDTO.hashCode() == hash;
    }

    static public String getJson(KeypleDTO keypleDTO){
        return SeProxyJsonParser.getGson().toJson(keypleDTO);
    }

    static public KeypleDTO parseJson(String json){
        return SeProxyJsonParser.getGson().fromJson(json, KeypleDTO.class);
    }

    static public KeypleDTO getEmptyDto(){
        return new KeypleDTO("noresult","",false);
    }

}
