/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.server.serializer.json;



import java.nio.ByteBuffer;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.SeRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class SeProxyJsonParser {

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
