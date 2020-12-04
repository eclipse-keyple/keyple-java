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

/**
 * Serializer/Deserializer of a {@link CardCommand}.
 *
 * @since 1.0
 */
public class CardCommandJsonAdapter
    implements JsonSerializer<CardCommand>, JsonDeserializer<CardCommand> {

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public JsonElement serialize(
      CardCommand cardCommand, Type type, JsonSerializationContext jsonSerializationContext) {

    JsonObject output = new JsonObject();
    Assert.getInstance()
        .isTrue(cardCommand.getClass().isEnum(), "CardCommandJsonAdapter works only with enum");
    output.addProperty("name", ((Enum) cardCommand).name());
    output.addProperty("class", cardCommand.getClass().getName());
    return output;
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
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
          "Can not parse jsonElement as a CardCommand " + jsonElement.toString());
    }
  }
}
