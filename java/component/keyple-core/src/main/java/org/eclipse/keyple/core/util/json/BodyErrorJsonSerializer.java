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

/**
 * Serializer of a {@link BodyError} that contains a {@link RuntimeException}.
 *
 * @since 1.0
 */
public class BodyErrorJsonSerializer implements JsonDeserializer<BodyError> {

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public BodyError deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {

    String exceptionName = jsonElement.getAsJsonObject().get("code").getAsString();
    JsonObject bodyException = jsonElement.getAsJsonObject().get("exception").getAsJsonObject();

    try {
      Class<RuntimeException> exceptionClass =
          (Class<RuntimeException>) Class.forName(exceptionName);
      return new BodyError(
          (RuntimeException) jsonDeserializationContext.deserialize(bodyException, exceptionClass));
    } catch (Throwable e) {
      throw new JsonParseException(e);
    }
  }
}
