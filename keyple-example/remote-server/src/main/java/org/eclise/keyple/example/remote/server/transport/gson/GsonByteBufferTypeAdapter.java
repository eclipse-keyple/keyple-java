package org.eclise.keyple.example.remote.server.transport.gson;

import com.google.gson.*;
import org.eclipse.keyple.util.ByteBufferUtils;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;

public class GsonByteBufferTypeAdapter implements JsonDeserializer<ByteBuffer>, JsonSerializer<ByteBuffer> {

    @Override
    public JsonElement serialize(ByteBuffer src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(ByteBufferUtils.toHex(src));
    }

    @Override
    public ByteBuffer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return  ByteBufferUtils.fromHex(json.getAsString());
    }


}
