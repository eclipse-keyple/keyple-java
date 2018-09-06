package org.eclise.keyple.example.remote.server.transport.gson;

import com.google.gson.*;
import org.eclipse.keyple.seproxy.SeRequest;
import org.eclipse.keyple.util.ByteBufferUtils;

import java.lang.reflect.Type;

public class GsonSelectorTypeAdapter implements JsonDeserializer<SeRequest.Selector>, JsonSerializer<SeRequest.Selector> {

    @Override
    public JsonElement serialize(SeRequest.Selector src, Type typeOfSrc, JsonSerializationContext context) {
        if(src instanceof SeRequest.AidSelector){
            return new JsonPrimitive("aidselector::"+ ByteBufferUtils.toHex(((SeRequest.AidSelector) src).getAidToSelect()));
        }else{
            return new JsonPrimitive("atrselector::"+((SeRequest.AtrSelector) src).getAtrRegex());
        }
    }

    @Override
    public SeRequest.Selector deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String element = json.getAsString();
        if(element.startsWith("atrselector::")){
            String regex = element.replace("atrselector::","");
            return new SeRequest.AtrSelector(regex);
        }else{
            if(element.startsWith("aidselector::")){
                String aidToSelect = element.replace("aidselector::","");
                return new SeRequest.AidSelector(ByteBufferUtils.fromHex(aidToSelect));

            }else{
                throw new JsonParseException("SeRequest.Selector malformed");
            }
        }
    }
}
