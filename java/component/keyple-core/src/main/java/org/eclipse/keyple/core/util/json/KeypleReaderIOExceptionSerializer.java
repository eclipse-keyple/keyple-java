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
import java.util.List;
import org.eclipse.keyple.core.card.message.CardResponse;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;

/** Remove Stacktrace and SupressedExceptions fields from serialization */
public class KeypleReaderIOExceptionSerializer implements JsonSerializer<KeypleReaderIOException> {

  @Override
  public JsonElement serialize(
      KeypleReaderIOException exception,
      Type type,
      JsonSerializationContext jsonSerializationContext) {
    JsonObject json = new JsonObject();
    json.add(
        "cardResponse",
        jsonSerializationContext.serialize(exception.getCardResponse(), CardResponse.class));
    json.add(
        "cardSelectionResponses",
        jsonSerializationContext.serialize(exception.getCardSelectionResponses(), List.class));
    json.addProperty("message", exception.getMessage());
    return json;
  }
}
