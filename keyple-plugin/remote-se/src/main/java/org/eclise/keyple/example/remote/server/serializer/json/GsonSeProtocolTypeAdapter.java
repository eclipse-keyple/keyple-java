/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.server.serializer.json;


import java.lang.reflect.Type;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.seproxy.protocol.ContactsProtocols;
import com.google.gson.*;

public class GsonSeProtocolTypeAdapter
        implements JsonDeserializer<SeProtocol>, JsonSerializer<SeProtocol> {

    @Override
    public JsonElement serialize(SeProtocol src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getName());
    }

    @Override
    public SeProtocol deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {

        String value = json.getAsString();

        for (ContactlessProtocols p : ContactlessProtocols.values()) {
            if (p.name().equals(value)) {
                return p;
            }
        }

        for (ContactsProtocols p : ContactsProtocols.values()) {
            if (p.name().equals(value)) {
                return p;
            }
        }
        throw new JsonParseException("Value of SeProtocol not found : " + value);



    }


}
