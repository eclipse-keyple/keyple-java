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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.eclipse.keyple.core.command.SeCommand;
import org.eclipse.keyple.core.command.exception.KeypleSeCommandException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.SeResponse;

import java.lang.reflect.Type;
import java.util.List;

public class KeypleSeCommandExceptionSerializer
        implements JsonSerializer<KeypleSeCommandException> {

    @Override
    public JsonElement serialize(KeypleSeCommandException exception, Type type,
            JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("command", jsonSerializationContext.serialize(exception.getCommand(), SeCommand.class));
        jsonObject.addProperty("statusCode", exception.getStatusCode());
        jsonObject.addProperty("message", exception.getMessage());
        jsonObject.addProperty("code", exception.getErrorCode());
        return jsonObject;
    }
}
