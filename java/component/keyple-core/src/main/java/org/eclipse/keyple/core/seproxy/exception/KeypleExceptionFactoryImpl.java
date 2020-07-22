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
package org.eclipse.keyple.core.seproxy.exception;

import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import com.google.gson.JsonObject;

public class KeypleExceptionFactoryImpl implements KeypleExceptionFactory {


    @Override
    public KeypleException from(String json) {

        JsonObject jsonObject = KeypleJsonParser.getParser().fromJson(json, JsonObject.class);
        String errorCode = jsonObject.get("code").getAsString();

        try {
            return (KeypleException) KeypleJsonParser.getParser().fromJson(json,
                    Class.forName(errorCode));
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("errorCode not found in this factory");
        }
    }
}
