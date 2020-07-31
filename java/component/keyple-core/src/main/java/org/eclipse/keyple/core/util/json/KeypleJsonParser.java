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
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Json Parser for Keyple DTO and Keyple DTO body
 */
public final class KeypleJsonParser {

    private static volatile Gson parser;
    private static final GsonBuilder gsonBuilder = initGsonBuider();

    /**
     * Get the singleton instance of the keyple gson parser. If not created yet, a default keyple
     * gson parser instance is created
     * 
     * @return singleton instance of gson
     */
    public static Gson getParser() {
        if (parser == null) {
            // init parser with keyple default value
            parser = gsonBuilder.create();
        }
        return parser;
    }

    private KeypleJsonParser() {}


    /**
     * Initialize and personalize the gson parser used in Keyple.
     * 
     * @return builder instance
     */
    private static GsonBuilder initGsonBuider() {
        GsonBuilder init = new GsonBuilder();
        // init keyple default adapter
        init.registerTypeAdapter(SeProtocol.class, new GsonSeProtocolTypeAdapter())
                .registerTypeAdapter(byte[].class, new HexArrayTypeAdapter())
                .registerTypeAdapter(SeCommand.class, new SeCommandTypeAdapter())
                .registerTypeAdapter(BodyError.class, new BodyErrorSerializer())
                .registerTypeHierarchyAdapter(Throwable.class, new ThrowableSerializer())
                .registerTypeAdapter(KeypleReaderIOException.class,
                        new KeypleReaderIOExceptionSerializer())
                .registerTypeHierarchyAdapter(KeypleSeCommandException.class,
                        new KeypleSeCommandExceptionSerializer());

        return init;
    }

    /**
     * Register a new type adapter
     *
     * @param matchingClass non nullable instance of the type to be registered
     * @param adapter non nullable of the type adapter to be registered (should implement
     *        {@link com.google.gson.JsonSerializer} and/or
     *        {@link com.google.gson.JsonDeserializer})
     * @param withSubclass apply this adapter to subclass of matchingClass also
     * @return updated gson instance
     */
    public static Gson registerTypeAdapter(Class matchingClass, Object adapter,
            Boolean withSubclass) {
        // init custom types after allowing the user to overwrite keyple default adapter
        if (withSubclass) {
            gsonBuilder.registerTypeHierarchyAdapter(matchingClass, adapter);
        } else {
            gsonBuilder.registerTypeAdapter(matchingClass, adapter);
        }
        parser = gsonBuilder.create();
        return parser;
    }
}
