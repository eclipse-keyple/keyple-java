/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.remote_se.transport.json;

import java.lang.reflect.Type;
import org.eclipse.keyple.seproxy.SeRequest;
import org.eclipse.keyple.util.ByteBufferUtils;
import com.google.gson.*;

public class GsonSelectorTypeAdapter
        implements JsonDeserializer<SeRequest.Selector>, JsonSerializer<SeRequest.Selector> {

    @Override
    public JsonElement serialize(SeRequest.Selector src, Type typeOfSrc,
            JsonSerializationContext context) {
        if (src instanceof SeRequest.AidSelector) {
            return new JsonPrimitive("aidselector::"
                    + ByteBufferUtils.toHex(((SeRequest.AidSelector) src).getAidToSelect()));
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
                return new SeRequest.AidSelector(ByteBufferUtils.fromHex(aidToSelect));

            } else {
                throw new JsonParseException("SeRequest.Selector malformed");
            }
        }
    }
}
