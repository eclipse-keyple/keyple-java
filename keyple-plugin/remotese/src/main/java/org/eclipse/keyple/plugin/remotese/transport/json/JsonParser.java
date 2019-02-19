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


import java.nio.ByteBuffer;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.SeRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * Json Parser for Keyple DTO and Keyple DTO fields
 */
public class JsonParser {

    static public Gson getGson() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ByteBuffer.class, new GsonByteBufferTypeAdapter());
        gsonBuilder.registerTypeAdapter(SeRequest.Selector.class, new GsonSelectorTypeAdapter());
        gsonBuilder.registerTypeAdapter(SeProtocol.class, new GsonSeProtocolTypeAdapter());
        gsonBuilder.setPrettyPrinting();
        return gsonBuilder.create();
    }

    static public Boolean isSeRequestSet(JsonObject obj) {
        return obj.get("sortedRequests") != null;
    }

}
