/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.remotese.transport.json;

import java.lang.reflect.Type;
import org.eclipse.keyple.seproxy.SeRequest;
import org.eclipse.keyple.util.ByteArrayUtils;
import com.google.gson.*;

class GsonSelectorTypeAdapter
        implements JsonDeserializer<SeRequest.Selector>, JsonSerializer<SeRequest.Selector> {

    @Override
    public JsonElement serialize(SeRequest.Selector src, Type typeOfSrc,
            JsonSerializationContext context) {
        if (src instanceof SeRequest.AidSelector) {
            return new JsonPrimitive("aidselector::"
                    + ByteArrayUtils.toHex(((SeRequest.AidSelector) src).getAidToSelect()));
        } else {
            return new JsonPrimitive("atrselector::" + ((SeRequest.AtrSelector) src).getAtrRegex());
        }
    }

    @Override
    public SeRequest.Selector deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        String element = json.getAsString();
        if (element.startsWith("atrselector::")) {
            String regex = element.replace("atrselector::", "");
            return new SeRequest.AtrSelector(regex);
        } else {
            if (element.startsWith("aidselector::")) {
                String aidToSelect = element.replace("aidselector::", "");
                return new SeRequest.AidSelector(ByteArrayUtils.fromHex(aidToSelect));

            } else {
                throw new JsonParseException("SeRequest.Selector malformed");
            }
        }
    }
}
