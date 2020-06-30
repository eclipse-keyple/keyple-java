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
package org.eclipse.keyple.plugin.remotese.core.impl.json;


import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Json Parser for Keyple DTO and Keyple DTO fields
 */
public class KeypleJsonParser {

    public static Gson getGson() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(SeProtocol.class, new GsonSeProtocolTypeAdapter());
        gsonBuilder.registerTypeAdapter(byte[].class, new HexTypeAdapter());
        // gsonBuilder.setPrettyPrinting(); disable pretty printing for inline json
        return gsonBuilder.create();
    }

}