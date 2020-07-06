/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.core.util.json.JsonAdapter;

import java.lang.reflect.Type;

public class CalypsoPoJson implements JsonAdapter<CalypsoPo> {
    @Override
    public CalypsoPo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return null;
    }

    @Override
    public JsonElement serialize(CalypsoPo src, Type typeOfSrc, JsonSerializationContext context) {




        return null;
    }
}
