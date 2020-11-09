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
package org.eclipse.keyple.plugin.remote.nativ.impl;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.core.card.message.*;
import org.eclipse.keyple.core.card.selection.MultiSelectionProcessing;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.util.json.BodyError;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remote.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remote.core.impl.AbstractKeypleMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (package-private)<br>
 * Abstract class for all Native Services.
 */
abstract class AbstractNativeService extends AbstractKeypleMessageHandler {

  private static final Logger logger = LoggerFactory.getLogger(AbstractNativeService.class);

  /**
   * (protected)<br>
   * Find a local reader among all plugins
   *
   * @param nativeReaderName name of the reader to be found
   * @return a not null instance
   * @throws KeypleReaderNotFoundException if no reader is found with this name
   * @since 1.0
   */
  protected ProxyReader findLocalReader(String nativeReaderName) {

    if (logger.isTraceEnabled()) {
      logger.trace(
          "Try to find local reader by name '{}' in {} plugin(s)",
          nativeReaderName,
          SmartCardService.getInstance().getPlugins().size());
    }

    for (Plugin plugin : SmartCardService.getInstance().getPlugins().values()) {
      try {
        if (logger.isTraceEnabled()) {
          logger.trace(
              "Try to find local reader '{}' in plugin '{}'", nativeReaderName, plugin.getName());
        }
        return (ProxyReader) plugin.getReader(nativeReaderName);
      } catch (KeypleReaderNotFoundException e) {
        // reader has not been found in this plugin, continue
      }
    }
    throw new KeypleReaderNotFoundException(nativeReaderName);
  }

  /**
   * (protected)<br>
   * Execute a keypleMessageDto on the local nativeReader, returns the response embedded on a
   * keypleMessageDto ready to be sent back.
   *
   * @param msg The message to process (must be not null).
   * @return a not null reference
   */
  protected KeypleMessageDto executeLocally(ProxyReader nativeReader, KeypleMessageDto msg) {
    return new NativeReaderExecutor(nativeReader, msg).execute();
  }

  /**
   * (private)<br>
   * Nested class used to execute the action on the native reader.
   */
  private static final class NativeReaderExecutor {

    private final ProxyReader reader;
    private final KeypleMessageDto msg;
    private final KeypleMessageDto.Action action;

    private NativeReaderExecutor(ProxyReader reader, KeypleMessageDto msg) {
      this.reader = reader;
      this.msg = msg;
      this.action = KeypleMessageDto.Action.valueOf(msg.getAction());
    }

    /**
     * (private)<br>
     * The main method.
     *
     * @return not null reference which can eventually contain an exception.
     */
    private KeypleMessageDto execute() {

      KeypleMessageDto response;
      try {
        switch (action) {
          case TRANSMIT:
            response = transmit();
            break;
          case TRANSMIT_CARD_SELECTION:
            response = transmitCardSelectionRequests();
            break;
          case SET_DEFAULT_SELECTION:
            response = setDefaultSelection();
            break;
          case IS_CARD_PRESENT:
            response = isCardPresent();
            break;
          case IS_READER_CONTACTLESS:
            response = isReaderContactless();
            break;
          case START_CARD_DETECTION:
            response = startCardDetection();
            break;
          case STOP_CARD_DETECTION:
            response = stopCardDetection();
            break;
          case FINALIZE_CARD_PROCESSING:
            response = finalizeCardProcessing();
            break;
          case RELEASE_CHANNEL:
            response = releaseChannel();
            break;
          default:
            throw new IllegalArgumentException(action.name());
        }
      } catch (KeypleReaderIOException e) {
        response =
            new KeypleMessageDto(msg) //
                .setAction(KeypleMessageDto.Action.ERROR.name()) //
                .setBody(KeypleJsonParser.getParser().toJson(new BodyError(e)));
      }
      return response;
    }

    /**
     * (private)<br>
     * Transmit
     *
     * @return a not null reference.
     * @throws KeypleReaderIOException if a reader IO error occurs.
     */
    private KeypleMessageDto transmit() {

      // Extract info from the message
      JsonObject bodyObject =
          KeypleJsonParser.getParser().fromJson(msg.getBody(), JsonObject.class);

      ChannelControl channelControl =
          ChannelControl.valueOf(bodyObject.get("channelControl").getAsString());

      CardRequest cardRequest =
          KeypleJsonParser.getParser()
              .fromJson(bodyObject.get("cardRequest").getAsString(), CardRequest.class);

      if (logger.isTraceEnabled()) {
        logger.trace(
            "Execute locally cardRequest : {} with params {} on reader {}",
            cardRequest,
            channelControl,
            reader.getName());
      }

      // Execute the action on the reader
      CardResponse cardResponse = reader.transmitCardRequest(cardRequest, channelControl);

      // Build response
      String body = KeypleJsonParser.getParser().toJson(cardResponse, CardResponse.class);
      return new KeypleMessageDto(msg).setBody(body);
    }

    /**
     * (private)<br>
     * Transmit Set
     *
     * @return a not null reference.
     * @throws KeypleReaderIOException if a reader IO error occurs.
     */
    private KeypleMessageDto transmitCardSelectionRequests() {

      // Extract info from the message
      JsonObject bodyJsonO = KeypleJsonParser.getParser().fromJson(msg.getBody(), JsonObject.class);

      List<CardSelectionRequest> cardSelectionRequests =
          KeypleJsonParser.getParser()
              .fromJson(
                  bodyJsonO.get("cardSelectionRequests").getAsString(),
                  new TypeToken<ArrayList<CardSelectionRequest>>() {}.getType());

      MultiSelectionProcessing multiSelectionProcessing =
          MultiSelectionProcessing.valueOf(bodyJsonO.get("multiSelectionProcessing").getAsString());

      ChannelControl channelControl =
          ChannelControl.valueOf(bodyJsonO.get("channelControl").getAsString());

      if (logger.isTraceEnabled()) {
        logger.trace(
            "Execute locally cardSelectionRequests : {} with params {} {}",
            cardSelectionRequests,
            channelControl,
            multiSelectionProcessing);
      }

      // Execute the action on the reader
      List<CardSelectionResponse> cardSelectionResponses =
          reader.transmitCardSelectionRequests(
              cardSelectionRequests, multiSelectionProcessing, channelControl);

      // Build response
      String body =
          KeypleJsonParser.getParser()
              .toJson(
                  cardSelectionResponses,
                  new TypeToken<ArrayList<CardSelectionResponse>>() {}.getType());
      return new KeypleMessageDto(msg).setBody(body);
    }

    /**
     * (private)<br>
     * Set Default Selection
     *
     * @return a not null reference.
     * @throws KeypleReaderIOException if a reader IO error occurs.
     */
    private KeypleMessageDto setDefaultSelection() {

      ObservableReader reader = (ObservableReader) this.reader;

      // Extract info from the message
      JsonObject body = KeypleJsonParser.getParser().fromJson(msg.getBody(), JsonObject.class);

      DefaultSelectionsRequest defaultSelectionsRequest =
          KeypleJsonParser.getParser()
              .fromJson(body.get("defaultSelectionsRequest"), DefaultSelectionsRequest.class);

      ObservableReader.NotificationMode notificationMode =
          ObservableReader.NotificationMode.valueOf(body.get("notificationMode").getAsString());

      // Polling Mode can be set or not.
      boolean hasPollingMode = false;
      ObservableReader.PollingMode pollingMode = null;

      String pollingModeJson =
          body.has("pollingMode") ? body.get("pollingMode").getAsString() : null;
      if (pollingModeJson != null) {
        hasPollingMode = true;
        pollingMode = ObservableReader.PollingMode.valueOf(pollingModeJson);
      }

      if (logger.isTraceEnabled()) {
        logger.trace(
            "Execute locally set DefaultSelectionExecutor on reader : {} with params {} {} {}",
            reader.getName(),
            defaultSelectionsRequest,
            notificationMode != null ? notificationMode : "no-notificationMode",
            hasPollingMode ? pollingMode : "no-pollingMode");
      }

      // Execute the action on the reader
      if (hasPollingMode) {
        reader.setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode, pollingMode);
      } else {
        reader.setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode);
      }

      // Build response
      return new KeypleMessageDto(msg).setBody(null);
    }

    /**
     * (private)<br>
     * Is SE Present ?
     *
     * @return a not null reference.
     * @throws KeypleReaderIOException if a reader IO error occurs.
     */
    private KeypleMessageDto isCardPresent() {

      // Execute the action on the reader
      boolean isSePresent = reader.isCardPresent();

      // Build response
      String body = KeypleJsonParser.getParser().toJson(isSePresent, Boolean.class);
      return new KeypleMessageDto(msg).setBody(body);
    }

    /**
     * (private)<br>
     * Is Reader Contactless ?
     *
     * @return a not null reference.
     * @throws KeypleReaderIOException if a reader IO error occurs.
     */
    private KeypleMessageDto isReaderContactless() {

      // Execute the action on the reader
      boolean isContactless = reader.isContactless();

      // Build response
      String body = KeypleJsonParser.getParser().toJson(isContactless, Boolean.class);
      return new KeypleMessageDto(msg).setBody(body);
    }

    /**
     * (private)<br>
     * Start SE Detection
     *
     * @return a not null reference.
     * @throws KeypleReaderIOException if a reader IO error occurs.
     */
    private KeypleMessageDto startCardDetection() {

      // Extract info from the message
      JsonObject body = KeypleJsonParser.getParser().fromJson(msg.getBody(), JsonObject.class);

      ObservableReader.PollingMode pollingMode =
          ObservableReader.PollingMode.valueOf(body.get("pollingMode").getAsString());

      // Execute the action on the reader
      ((ObservableReader) reader).startCardDetection(pollingMode);

      return new KeypleMessageDto(msg).setBody(null);
    }

    /**
     * (private)<br>
     * Stop SE Detection
     *
     * @return a not null reference.
     * @throws KeypleReaderIOException if a reader IO error occurs.
     */
    private KeypleMessageDto stopCardDetection() {

      // Execute the action on the reader
      ((ObservableReader) reader).stopCardDetection();

      return new KeypleMessageDto(msg).setBody(null);
    }

    /**
     * (private)<br>
     * Finalize SE Processing
     *
     * @return a not null reference.
     * @throws KeypleReaderIOException if a reader IO error occurs.
     */
    private KeypleMessageDto finalizeCardProcessing() {

      // Execute the action on the reader
      ((ObservableReader) reader).finalizeCardProcessing();

      return new KeypleMessageDto(msg).setBody(null);
    }

    /**
     * (private)<br>
     * Release channel
     *
     * @return a not null reference.
     * @throws KeypleReaderIOException if a reader IO error occurs.
     */
    private KeypleMessageDto releaseChannel() {

      // Execute the action on the reader
      reader.releaseChannel();

      return new KeypleMessageDto(msg).setBody(null);
    }
  }
}
