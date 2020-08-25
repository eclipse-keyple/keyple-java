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
package org.eclipse.keyple.core.util.json;


import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import com.google.gson.*;

/**
 * SE Protocol Setting Serializer
 */
public class SeProtocolSettingSerializer implements JsonSerializer<Map<SeProtocol, String>> {

    @Override
    public JsonElement serialize(Map<SeProtocol, String> src, Type typeOfSrc,
            JsonSerializationContext context) {
        Map<String, String> target = new HashMap<String, String>();
        for (Map.Entry<SeProtocol, String> entry : src.entrySet()) {
            target.put(entry.getKey().getName(), entry.getValue());
        }
        return context.serialize(target);
    }
}
