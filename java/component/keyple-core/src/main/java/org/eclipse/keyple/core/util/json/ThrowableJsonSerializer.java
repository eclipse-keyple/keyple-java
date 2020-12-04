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

/**
 * Serializer of a {@link java.lang.Throwable}.
 *
 * <p>Only the field "message" is serialized during the process.
 *
 * @since 1.0
 */
public class ThrowableJsonSerializer implements JsonSerializer<Throwable> {

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public JsonElement serialize(
      Throwable exception, Type type, JsonSerializationContext jsonSerializationContext) {

    JsonObject json = new JsonObject();
    json.addProperty("message", exception.getMessage());
    return json;
  }
}
