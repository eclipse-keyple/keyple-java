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

import com.google.gson.*;
import java.lang.reflect.Type;
import org.eclipse.keyple.core.card.command.CardCommand;
import org.eclipse.keyple.core.util.Assert;

public class SeCommandTypeAdapter
    implements JsonSerializer<CardCommand>, JsonDeserializer<CardCommand> {

  @Override
  public CardCommand deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {

    String className = jsonElement.getAsJsonObject().get("class").getAsString();
    String name = jsonElement.getAsJsonObject().get("name").getAsString();

    try {
      return (CardCommand) Enum.valueOf((Class<? extends Enum>) Class.forName(className), name);
    } catch (ClassNotFoundException e) {
      throw new JsonParseException(
          "Can not parse jsonElement as a SeCommand " + jsonElement.toString());
    }
  }

  @Override
  public JsonElement serialize(
          CardCommand seCommand, Type type, JsonSerializationContext jsonSerializationContext) {
    JsonObject output = new JsonObject();
    Assert.getInstance()
        .isTrue(seCommand.getClass().isEnum(), "SeCommandAdapter works only with enum");
    output.addProperty("name", ((Enum) seCommand).name());
    output.addProperty("class", seCommand.getClass().getName());
    return output;
  }
}
