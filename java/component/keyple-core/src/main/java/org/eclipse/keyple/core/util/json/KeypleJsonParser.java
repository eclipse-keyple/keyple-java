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


import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Json Parser for Keyple DTO and Keyple DTO body
 */
public class KeypleJsonParser {

    final private Gson parser;

    private KeypleJsonParser() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(SeProtocol.class, new GsonSeProtocolTypeAdapter());
        gsonBuilder.registerTypeAdapter(byte[].class, new HexArrayTypeAdapter());
        // gsonBuilder.setPrettyPrinting(); disable pretty printing for inline json
        parser = gsonBuilder.create();
    }

    private static class GsonParser {
        private static final KeypleJsonParser INSTANCE = new KeypleJsonParser();
    }

    public static Gson getParser() {
        return GsonParser.INSTANCE.getGson();
    }

    private Gson getGson() {
        return parser;
    }

}
