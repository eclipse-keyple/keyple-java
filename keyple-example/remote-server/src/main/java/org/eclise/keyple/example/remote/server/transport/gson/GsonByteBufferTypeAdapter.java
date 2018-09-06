/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.server.transport.gson;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import org.eclipse.keyple.util.ByteBufferUtils;
import com.google.gson.*;

public class GsonByteBufferTypeAdapter
        implements JsonDeserializer<ByteBuffer>, JsonSerializer<ByteBuffer> {

    @Override
    public JsonElement serialize(ByteBuffer src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(ByteBufferUtils.toHex(src));
    }

    @Override
    public ByteBuffer deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        return ByteBufferUtils.fromHex(json.getAsString());
    }


}
