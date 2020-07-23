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


import org.eclipse.keyple.core.command.SeCommand;
import org.eclipse.keyple.core.command.exception.KeypleSeCommandException;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Json Parser for Keyple DTO and Keyple DTO body
 */
public class KeypleJsonParser {

    private Gson parser;

    private KeypleJsonParser() {
        GsonBuilder gsonBuilder = initGsonBuilder();
        // gsonBuilder.setPrettyPrinting(); disable pretty printing for inline json
        parser = gsonBuilder.create();
    }

    private static class GsonParser {
        private static final KeypleJsonParser INSTANCE = new KeypleJsonParser();
    }

    public static Gson getParser() {
        return GsonParser.INSTANCE.parser;
    }

    public void addAdapters(Map<Type, Object> typeAdapters) {
        GsonBuilder gsonBuilder = initGsonBuilder();
        for(Type typeAdapter : typeAdapters.keySet()){
            gsonBuilder.registerTypeAdapter(typeAdapter, typeAdapters.get(typeAdapter));
        }
        parser = gsonBuilder.create();
    }

    private GsonBuilder initGsonBuilder(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(SeProtocol.class, new GsonSeProtocolTypeAdapter());
        gsonBuilder.registerTypeAdapter(byte[].class, new HexArrayTypeAdapter());
        gsonBuilder.registerTypeAdapter(SeCommand.class, new SeCommandTypeAdapter());
        gsonBuilder.registerTypeAdapter(KeypleException.class, new KeypleExceptionTypeAdapter());
        gsonBuilder.registerTypeAdapter(KeypleReaderIOException.class, new KeypleReaderIOExceptionSerializer());
        gsonBuilder.registerTypeAdapter(KeypleSeCommandException.class, new KeypleSeCommandExceptionSerializer());
        return gsonBuilder;
    }

}
