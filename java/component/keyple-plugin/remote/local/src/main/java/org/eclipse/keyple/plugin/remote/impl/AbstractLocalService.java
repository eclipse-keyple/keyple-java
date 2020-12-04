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
package org.eclipse.keyple.plugin.remote.impl;

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
import org.eclipse.keyple.core.util.json.KeypleGsonParser;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (package-private)<br>
 * Abstract class for all Local Services.
 *
 * @since 1.0
 */
abstract class AbstractLocalService extends AbstractMessageHandler {

  private static final Logger logger = LoggerFactory.getLogger(AbstractLocalService.class);

  /**
   * (package-private)<br>
   * Find a local reader among all plugins
   *
   * @param localReaderName name of the reader to be found
   * @return a not null instance
   * @throws KeypleReaderNotFoundException if no reader is found with this name
   * @since 1.0
   */
  ProxyReader findLocalReader(String localReaderName) {

    if (logger.isTraceEnabled()) {
      logger.trace(
          "Try to find local reader by name '{}' in {} plugin(s)",
          localReaderName,
          SmartCardService.getInstance().getPlugins().size());
    }

    for (Plugin plugin : SmartCardService.getInstance().getPlugins().values()) {
      try {
        if (logger.isTraceEnabled()) {
          logger.trace(
              "Try to find local reader '{}' in plugin '{}'", localReaderName, plugin.getName());
        }
        return (ProxyReader) plugin.getReader(localReaderName);
      } catch (KeypleReaderNotFoundException e) {
        // reader has not been found in this plugin, continue
      }
    }
    throw new KeypleReaderNotFoundException(localReaderName);
  }

  /**
   * (package-private)<br>
   * Execute a messageDto on the local localReader, returns the response embedded on a messageDto
   * ready to be sent back.
   *
   * @param msg The message to process (must be not null).
   * @return a not null reference
   * @since 1.0
   */
  MessageDto executeLocally(ProxyReader localReader, MessageDto msg) {
    return new LocalReaderExecutor(localReader, msg).execute();
  }

  /**
   * (private)<br>
   * Nested class used to execute the action on the local reader.
   */
  private static final class LocalReaderExecutor {

    private final ProxyReader reader;
    private final MessageDto msg;
    private final MessageDto.Action action;

    private LocalReaderExecutor(ProxyReader reader, MessageDto msg) {
      this.reader = reader;
      this.msg = msg;
      this.action = MessageDto.Action.valueOf(msg.getAction());
    }

    /**
     * (private)<br>
     * The main method.
     *
     * @return not null reference which can eventually contain an exception.
     */
    private MessageDto execute() {

      MessageDto response;
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
            new MessageDto(msg) //
                .setAction(MessageDto.Action.ERROR.name()) //
                .setBody(KeypleGsonParser.getParser().toJson(new BodyError(e)));
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
    private MessageDto transmit() {

      // Extract info from the message
      JsonObject bodyObject =
          KeypleGsonParser.getParser().fromJson(msg.getBody(), JsonObject.class);

      ChannelControl channelControl =
          ChannelControl.valueOf(bodyObject.get("channelControl").getAsString());

      CardRequest cardRequest =
          KeypleGsonParser.getParser()
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
      String body = KeypleGsonParser.getParser().toJson(cardResponse, CardResponse.class);
      return new MessageDto(msg).setBody(body);
    }

    /**
     * (private)<br>
     * Transmit Set
     *
     * @return a not null reference.
     * @throws KeypleReaderIOException if a reader IO error occurs.
     */
    private MessageDto transmitCardSelectionRequests() {

      // Extract info from the message
      JsonObject bodyJsonO = KeypleGsonParser.getParser().fromJson(msg.getBody(), JsonObject.class);

      List<CardSelectionRequest> cardSelectionRequests =
          KeypleGsonParser.getParser()
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
          KeypleGsonParser.getParser()
              .toJson(
                  cardSelectionResponses,
                  new TypeToken<ArrayList<CardSelectionResponse>>() {}.getType());
      return new MessageDto(msg).setBody(body);
    }

    /**
     * (private)<br>
     * Set Default Selection
     *
     * @return a not null reference.
     * @throws KeypleReaderIOException if a reader IO error occurs.
     */
    private MessageDto setDefaultSelection() {

      ObservableReader reader = (ObservableReader) this.reader;

      // Extract info from the message
      JsonObject body = KeypleGsonParser.getParser().fromJson(msg.getBody(), JsonObject.class);

      DefaultSelectionsRequest defaultSelectionsRequest =
          KeypleGsonParser.getParser()
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
      return new MessageDto(msg).setBody(null);
    }

    /**
     * (private)<br>
     * Is SE Present ?
     *
     * @return a not null reference.
     * @throws KeypleReaderIOException if a reader IO error occurs.
     */
    private MessageDto isCardPresent() {

      // Execute the action on the reader
      boolean isSePresent = reader.isCardPresent();

      // Build response
      String body = KeypleGsonParser.getParser().toJson(isSePresent, Boolean.class);
      return new MessageDto(msg).setBody(body);
    }

    /**
     * (private)<br>
     * Is Reader Contactless ?
     *
     * @return a not null reference.
     * @throws KeypleReaderIOException if a reader IO error occurs.
     */
    private MessageDto isReaderContactless() {

      // Execute the action on the reader
      boolean isContactless = reader.isContactless();

      // Build response
      String body = KeypleGsonParser.getParser().toJson(isContactless, Boolean.class);
      return new MessageDto(msg).setBody(body);
    }

    /**
     * (private)<br>
     * Start SE Detection
     *
     * @return a not null reference.
     * @throws KeypleReaderIOException if a reader IO error occurs.
     */
    private MessageDto startCardDetection() {

      // Extract info from the message
      JsonObject body = KeypleGsonParser.getParser().fromJson(msg.getBody(), JsonObject.class);

      ObservableReader.PollingMode pollingMode =
          ObservableReader.PollingMode.valueOf(body.get("pollingMode").getAsString());

      // Execute the action on the reader
      ((ObservableReader) reader).startCardDetection(pollingMode);

      return new MessageDto(msg).setBody(null);
    }

    /**
     * (private)<br>
     * Stop SE Detection
     *
     * @return a not null reference.
     * @throws KeypleReaderIOException if a reader IO error occurs.
     */
    private MessageDto stopCardDetection() {

      // Execute the action on the reader
      ((ObservableReader) reader).stopCardDetection();

      return new MessageDto(msg).setBody(null);
    }

    /**
     * (private)<br>
     * Finalize SE Processing
     *
     * @return a not null reference.
     * @throws KeypleReaderIOException if a reader IO error occurs.
     */
    private MessageDto finalizeCardProcessing() {

      // Execute the action on the reader
      ((ObservableReader) reader).finalizeCardProcessing();

      return new MessageDto(msg).setBody(null);
    }

    /**
     * (private)<br>
     * Release channel
     *
     * @return a not null reference.
     * @throws KeypleReaderIOException if a reader IO error occurs.
     */
    private MessageDto releaseChannel() {

      // Execute the action on the reader
      reader.releaseChannel();

      return new MessageDto(msg).setBody(null);
    }
  }
}
