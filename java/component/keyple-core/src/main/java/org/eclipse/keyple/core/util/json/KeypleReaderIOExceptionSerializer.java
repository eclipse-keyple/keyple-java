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
import java.util.List;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import com.google.gson.*;

public class KeypleReaderIOExceptionSerializer implements JsonSerializer<KeypleReaderIOException> {

    @Override
    public JsonElement serialize(KeypleReaderIOException exception, Type type,
            JsonSerializationContext jsonSerializationContext) {
        JsonObject json = new JsonObject();
        json.add("seResponse",
                jsonSerializationContext.serialize(exception.getSeResponse(), SeResponse.class));
        json.add("seResponses",
                jsonSerializationContext.serialize(exception.getSeResponses(), List.class));
        json.addProperty("message", exception.getMessage());
        json.addProperty("code", exception.getErrorCode());
        return json;
    }
}
