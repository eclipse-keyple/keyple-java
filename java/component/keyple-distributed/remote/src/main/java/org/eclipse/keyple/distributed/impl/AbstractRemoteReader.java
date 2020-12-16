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
package org.eclipse.keyple.distributed.impl;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.util.*;
import org.eclipse.keyple.core.card.message.*;
import org.eclipse.keyple.core.card.selection.MultiSelectionProcessing;
import org.eclipse.keyple.core.plugin.AbstractReader;
import org.eclipse.keyple.core.util.json.BodyError;
import org.eclipse.keyple.core.util.json.KeypleGsonParser;
import org.eclipse.keyple.distributed.MessageDto;

/**
 * (package-private)<br>
 * Abstract Remote Reader
 *
 * <p>This object is a {@link AbstractReader} with additional features.
 *
 * @since 1.0
 */
abstract class AbstractRemoteReader extends AbstractReader {

  private final AbstractNode node;
  private final String clientNodeId;
  private final String localReaderName;
  private final String sessionId;

  /**
   * (package-private)<br>
   *
   * @param pluginName The name of the plugin (must be not null).
   * @param localReaderName The name of the local reader (must be not null).
   * @param node The associated node (must be not null).
   * @param sessionId Session Id (can be null)
   * @param clientNodeId Client node Id (can be null)
   * @since 1.0
   */
  AbstractRemoteReader(
      String pluginName,
      String localReaderName,
      AbstractNode node,
      String sessionId,
      String clientNodeId) {
    super(pluginName, UUID.randomUUID().toString());
    this.localReaderName = localReaderName;
    this.node = node;
    this.sessionId = sessionId;
    this.clientNodeId = clientNodeId;
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  protected List<CardSelectionResponse> processCardSelectionRequests(
      List<CardSelectionRequest> cardSelectionRequests,
      MultiSelectionProcessing multiSelectionProcessing,
      ChannelControl channelControl) {

    // Build the message
    JsonObject body = new JsonObject();
    body.addProperty(
        "cardSelectionRequests",
        KeypleGsonParser.getParser()
            .toJson(
                cardSelectionRequests,
                new TypeToken<ArrayList<CardSelectionRequest>>() {}.getType()));
    body.addProperty("multiSelectionProcessing", multiSelectionProcessing.name());
    body.addProperty("channelControl", channelControl.name());

    // Send the message as a request
    MessageDto response = sendRequest(MessageDto.Action.TRANSMIT_CARD_SELECTION, body);

    // Extract the response
    return KeypleGsonParser.getParser()
        .fromJson(
            response.getBody(), new TypeToken<ArrayList<CardSelectionResponse>>() {}.getType());
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  protected CardResponse processCardRequest(
      CardRequest cardRequest, ChannelControl channelControl) {

    // Build the message
    JsonObject body = new JsonObject();
    body.addProperty(
        "cardRequest", KeypleGsonParser.getParser().toJson(cardRequest, CardRequest.class));
    body.addProperty("channelControl", channelControl.name());

    // Send the message as a request
    MessageDto response = sendRequest(MessageDto.Action.TRANSMIT, body);

    // Extract the response
    return KeypleGsonParser.getParser().fromJson(response.getBody(), CardResponse.class);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public boolean isCardPresent() {

    // Send the message as a request
    MessageDto response = sendRequest(MessageDto.Action.IS_CARD_PRESENT, null);

    // Extract the response
    return KeypleGsonParser.getParser().fromJson(response.getBody(), Boolean.class);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void releaseChannel() {
    // Send the message as a request even if no return is expected
    sendRequest(MessageDto.Action.RELEASE_CHANNEL, null);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void activateProtocol(String readerProtocolName, String applicationProtocolName) {
    throw new UnsupportedOperationException(
        "activateProtocol method is not implemented in plugin remote, use it only locally");
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void deactivateProtocol(String readerProtocolName) {
    throw new UnsupportedOperationException(
        "activateProtocol method is not implemented in plugin remote, use it only locally");
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public boolean isContactless() {
    MessageDto response = sendRequest(MessageDto.Action.IS_READER_CONTACTLESS, null);

    // Extract the response
    return KeypleGsonParser.getParser().fromJson(response.getBody(), Boolean.class);
  }

  /**
   * (package-private)<br>
   * Sends a request.
   * <li>Build the message using the provided action and body.
   * <li>Send it as a request.
   * <li>If an error is contained in the response, then throw an exception.
   *
   * @param action The action (must be not null).
   * @param body The body (optional).
   * @return a not null reference.
   * @since 1.0
   */
  MessageDto sendRequest(MessageDto.Action action, JsonObject body) {

    // Build the message
    MessageDto message =
        new MessageDto() //
            .setSessionId(sessionId != null ? sessionId : UUID.randomUUID().toString()) //
            .setAction(action.name()) //
            .setRemoteReaderName(getName()) //
            .setLocalReaderName(localReaderName) //
            .setClientNodeId(clientNodeId) //
            .setBody(body != null ? body.toString() : null);

    // Send the message as a request
    MessageDto response = node.sendRequest(message);

    // Extract the response
    checkError(response);
    return response;
  }

  /**
   * (private)<br>
   * If message contains an error, throws the embedded exception.
   *
   * @param message The message to check (must be not null)
   */
  private void checkError(MessageDto message) {
    if (message.getAction().equals(MessageDto.Action.ERROR.name())) {
      BodyError body = KeypleGsonParser.getParser().fromJson(message.getBody(), BodyError.class);
      throw body.getException();
    }
  }

  /**
   * (package-private)<br>
   * Gets the current session id
   *
   * @return a nullable value.
   * @since 1.0
   */
  String getSessionId() {
    return sessionId;
  }

  /**
   * (package-private)<br>
   * Gets the current client id
   *
   * @return a nullable value.
   * @since 1.0
   */
  String getClientNodeId() {
    return clientNodeId;
  }

  /**
   * (package-private)<br>
   * Gets the local reader name
   *
   * @return non null instance of a localReaderName
   * @since 1.0
   */
  String getLocalReaderName() {
    return localReaderName;
  }
}
