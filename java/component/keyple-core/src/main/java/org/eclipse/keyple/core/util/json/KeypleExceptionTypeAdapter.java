/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.core.util.json;

import java.lang.reflect.Type;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import com.google.gson.*;

public class KeypleExceptionTypeAdapter
        implements JsonSerializer<KeypleException>, JsonDeserializer<KeypleException> {

    @Override
    public KeypleException deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String errorCode = jsonElement.getAsJsonObject().get("code").getAsString();
        // String message = jsonElement.getAsJsonObject().get("message").getAsString();
        try {
            Class exceptionClass = Class.forName(errorCode);
            return jsonDeserializationContext.deserialize(jsonElement, exceptionClass);
        } catch (Throwable e) {
            throw new JsonParseException(e);
        }
    }

    @Override
    public JsonElement serialize(KeypleException exception, Type type,
            JsonSerializationContext jsonSerializationContext) {
        JsonObject output = new JsonObject();
        output.addProperty("message", exception.getMessage());
        // output.addProperty("instructionByte", seCommand.getInstructionByte());
        output.addProperty("code", exception.getClass().getName());
        return output;
    }
}
