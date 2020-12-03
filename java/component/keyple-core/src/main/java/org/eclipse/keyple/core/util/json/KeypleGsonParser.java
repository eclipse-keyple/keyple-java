/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.util.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.keyple.core.card.command.CardCommand;
import org.eclipse.keyple.core.card.command.exception.KeypleCardCommandException;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;

/**
 * GSON Parser of Keyple objects.
 *
 * @since 1.0
 */
public final class KeypleGsonParser {

  private static volatile Gson parser;
  private static final GsonBuilder gsonBuilder = initGsonBuilder();

  /**
   * Gets the singleton instance of the parser.
   *
   * <p>If not created yet, a default instance is created.
   *
   * @return a not null instance.
   */
  public static Gson getParser() {
    if (parser == null) {
      // init parser with keyple default value
      parser = gsonBuilder.create();
    }
    return parser;
  }

  /**
   * (private)<br>
   * Constructor.
   */
  private KeypleGsonParser() {}

  /**
   * (private)<br>
   * Initialize and personalize the gson parser used in Keyple.
   *
   * @return a not null builder instance.
   */
  private static GsonBuilder initGsonBuilder() {
    GsonBuilder init = new GsonBuilder();
    init.registerTypeAdapter(byte[].class, new ByteArrayJsonAdapter())
        .registerTypeAdapter(CardCommand.class, new CardCommandJsonAdapter())
        .registerTypeAdapter(BodyError.class, new BodyErrorJsonSerializer())
        .registerTypeHierarchyAdapter(Throwable.class, new ThrowableJsonSerializer())
        .registerTypeAdapter(
            KeypleReaderIOException.class, new KeypleReaderIOExceptionJsonSerializer())
        .registerTypeHierarchyAdapter(
            KeypleCardCommandException.class, new KeypleCardCommandExceptionJsonSerializer());
    return init;
  }

  /**
   * Registers a new type adapter and returns the updated parser.
   *
   * @param matchingClass The type to be registered.
   * @param adapter The type adapter to be registered (should implement {@link
   *     com.google.gson.JsonSerializer} and/or {@link com.google.gson.JsonDeserializer}).
   * @param withSubclasses Apply this adapter to subclasses of matchingClass also.
   * @return a not null reference.
   * @since 1.0
   */
  public static Gson registerTypeAdapter(
      Class<?> matchingClass, Object adapter, boolean withSubclasses) {

    // init custom types after allowing the user to overwrite keyple default adapter
    if (withSubclasses) {
      gsonBuilder.registerTypeHierarchyAdapter(matchingClass, adapter);
    } else {
      gsonBuilder.registerTypeAdapter(matchingClass, adapter);
    }
    parser = gsonBuilder.create();
    return parser;
  }
}
