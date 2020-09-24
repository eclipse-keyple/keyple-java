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
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;

/** Gson Adapter to serialize and deserialize {@link SeProtocol} */
public class SeProtocolCommonSerializer implements JsonSerializer<SeCommonProtocols> {

  @Override
  public JsonElement serialize(
      SeCommonProtocols src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(src.getName());
  }
}
