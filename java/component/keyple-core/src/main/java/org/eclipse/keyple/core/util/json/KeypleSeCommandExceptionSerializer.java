/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.util.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import org.eclipse.keyple.core.card.command.CardCommand;
import org.eclipse.keyple.core.card.command.exception.KeypleCardCommandException;

/** Force the command field to be serialized as a SeCommand.class */
public class KeypleSeCommandExceptionSerializer
    implements JsonSerializer<KeypleCardCommandException> {

  @Override
  public JsonElement serialize(
      KeypleCardCommandException exception,
      Type type,
      JsonSerializationContext jsonSerializationContext) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.add(
        "command", jsonSerializationContext.serialize(exception.getCommand(), CardCommand.class));
    jsonObject.addProperty("statusCode", exception.getStatusCode());
    jsonObject.addProperty("message", exception.getMessage());
    return jsonObject;
  }
}
