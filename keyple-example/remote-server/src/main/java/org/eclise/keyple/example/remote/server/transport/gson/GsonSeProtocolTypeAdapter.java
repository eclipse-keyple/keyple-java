package org.eclise.keyple.example.remote.server.transport.gson;


import com.google.gson.*;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.seproxy.protocol.ContactsProtocols;

import java.lang.reflect.Type;

public class GsonSeProtocolTypeAdapter implements JsonDeserializer<SeProtocol>, JsonSerializer<SeProtocol> {

    @Override
    public JsonElement serialize(SeProtocol src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getName());
    }

    @Override
    public SeProtocol deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        String value = json.getAsString();

        for(ContactlessProtocols p : ContactlessProtocols.values()) {
            if(p.name().equals(value)){
                return p;
            }
        }

        for(ContactsProtocols p : ContactsProtocols.values()) {
            if(p.name().equals(value)){
                return p;
            }
        }
        throw  new JsonParseException("Value of SeProtocol not found : " +  value);







    }


}
