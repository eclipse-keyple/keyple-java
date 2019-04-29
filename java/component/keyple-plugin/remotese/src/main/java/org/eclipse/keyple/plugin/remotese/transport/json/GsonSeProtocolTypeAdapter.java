/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.remotese.transport.json;


import java.lang.reflect.Type;
import org.eclipse.keyple.core.seproxy.protocol.*;
import com.google.gson.*;

/**
 * Parse and unparse SeProtocol enums
 */
class GsonSeProtocolTypeAdapter
        implements JsonDeserializer<SeProtocol>, JsonSerializer<SeProtocol> {

    @Override
    public JsonElement serialize(SeProtocol src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getName());
    }

    @Override
    public SeProtocol deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {

        String value = json.getAsString();

        for (SeCommonProtocol p : SeCommonProtocol.values()) {
            if (p.name().equals(value)) {
                return p;
            }
        }

        throw new JsonParseException("Value of SeProtocol not found : " + value);
    }
}
