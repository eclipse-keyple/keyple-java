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
import org.eclipse.keyple.core.command.SeCommand;
import org.eclipse.keyple.core.command.exception.KeypleSeCommandException;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Json Parser for Keyple DTO and Keyple DTO body
 */
public class KeypleJsonParser {

    static private Gson parser;

    /**
     * Get the singleton instance of the keyple gson parser.
     * If not created yet, a default keyple gson parser instance is created

     * @return singleton instance of gson
     */
    static public Gson getParser() {
        if (parser == null) {
            // init parser with keyple default value
            parser = new BuildStep().getParser();
        }
        return parser;
    }

    private KeypleJsonParser() {}

    /**
     * Initialize and personalize the gson parser used in Keyple. If the singleton instance already existed, it will be overwritten when calling the method getParser.
     * @return builder instance
     */
    static public BuildStep build() {
        return new BuildStep();
    }

    public interface GsonBuildStep {

        /**
         * Build the keyple gson parser instance
         * @return instance of gson
         */
        Gson getParser();

        /**
         * Register a new type adapter
         * @param type non nullable instance of the type to be registered
         * @param adapter non nullable of the type adapter to be registered
         * @return builder step
         */
        GsonBuildStep registerTypeAdapter(Type type, Object adapter);
    }

    public static class BuildStep implements GsonBuildStep {

        final GsonBuilder gsonBuilder;

        private BuildStep() {
            gsonBuilder = new GsonBuilder();
            // init keyple default adapter
            gsonBuilder.registerTypeAdapter(SeProtocol.class, new GsonSeProtocolTypeAdapter())
                    .registerTypeAdapter(byte[].class, new HexArrayTypeAdapter())
                    .registerTypeAdapter(SeCommand.class, new SeCommandTypeAdapter())
                    .registerTypeAdapter(KeypleException.class, new KeypleExceptionTypeAdapter())
                    .registerTypeAdapter(KeypleReaderIOException.class, new KeypleReaderIOExceptionSerializer())
                    .registerTypeAdapter(KeypleSeCommandException.class, new KeypleSeCommandExceptionSerializer());
        }

        @Override
        public Gson getParser() {
            parser = gsonBuilder.create();
            return parser;
        }

        @Override
        public BuildStep registerTypeAdapter(Type type, Object adapter) {
            // init custom types after allowing the user to overwrite keyple default adapter
            gsonBuilder.registerTypeAdapter(type, adapter);
            return this;
        }
    }


}
