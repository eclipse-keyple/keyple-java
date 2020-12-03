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
import org.eclipse.keyple.core.util.ByteArrayUtil;

/**
 * Serializer/Deserializer of a byte array to an hex string.
 *
 * @since 1.0
 */
public class ByteArrayJsonAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {

  /** {@inheritDoc} */
  @Override
  public JsonElement serialize(byte[] data, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(ByteArrayUtil.toHex(data));
  }

  /** {@inheritDoc} */
  @Override
  public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    return ByteArrayUtil.fromHex(json.getAsString());
  }
}
