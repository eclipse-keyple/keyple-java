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
import com.google.gson.*;

/**
 * Serialize and Deserialize a {@link BodyError} that contains a RuntimeException
 */
public class BodyErrorSerializer
        implements JsonDeserializer<BodyError> {

    @Override
    public BodyError deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String exceptionName = jsonElement.getAsJsonObject().get("code").getAsString();
        JsonObject bodydException =
                jsonElement.getAsJsonObject().get("exception").getAsJsonObject();
        try {
            Class<RuntimeException> exceptionClass =
                    (Class<RuntimeException>) Class.forName(exceptionName);
            return new BodyError((RuntimeException)jsonDeserializationContext
                    .deserialize(bodydException, exceptionClass));
        } catch (Throwable e) {
            throw new JsonParseException(e);
        }
    }
}
