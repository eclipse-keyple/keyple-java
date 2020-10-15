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
package org.eclipse.keyple.plugin.remotese.virtualse.impl;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.util.*;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.message.ChannelControl;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.plugin.reader.AbstractReader;
import org.eclipse.keyple.core.util.json.BodyError;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remotese.core.impl.AbstractKeypleNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (package-private)<br>
 * Abstract Virtual Reader
 *
 * <p>This object is a {@link AbstractReader} with additional features.
 */
abstract class AbstractVirtualReader extends AbstractReader {

  private static final Logger logger = LoggerFactory.getLogger(AbstractVirtualReader.class);

  protected final AbstractKeypleNode node;
  protected final String nativeReaderName;

  private String sessionId;
  private String clientNodeId;

  /**
   * (package-private)<br>
   * Constructor
   *
   * @param pluginName The name of the plugin (must be not null).
   * @param nativeReaderName The name of the native reader (must be not null).
   * @param node The associated node (must be not null).
   * @param sessionId Session Id (can be null)
   * @param clientNodeId Client node Id (can be null)
   */
  AbstractVirtualReader(
      String pluginName,
      String nativeReaderName,
      AbstractKeypleNode node,
      String sessionId,
      String clientNodeId) {
    super(pluginName, UUID.randomUUID().toString());
    this.nativeReaderName = nativeReaderName;
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
  protected List<SeResponse> processSeRequests(
      List<SeRequest> seRequests,
      MultiSeRequestProcessing multiSeRequestProcessing,
      ChannelControl channelControl) {

    // Build the message
    JsonObject body = new JsonObject();
    body.addProperty(
        "seRequests",
        KeypleJsonParser.getParser()
            .toJson(seRequests, new TypeToken<ArrayList<SeRequest>>() {}.getType()));
    body.addProperty("multiSeRequestProcessing", multiSeRequestProcessing.name());
    body.addProperty("channelControl", channelControl.name());

    // Send the message as a request
    KeypleMessageDto response = sendRequest(KeypleMessageDto.Action.TRANSMIT_SET, body);

    // Extract the response
    return KeypleJsonParser.getParser()
        .fromJson(response.getBody(), new TypeToken<ArrayList<SeResponse>>() {}.getType());
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  protected SeResponse processSeRequest(SeRequest seRequest, ChannelControl channelControl) {

    // Build the message
    JsonObject body = new JsonObject();
    body.addProperty("seRequest", KeypleJsonParser.getParser().toJson(seRequest, SeRequest.class));
    body.addProperty("channelControl", channelControl.name());

    // Send the message as a request
    KeypleMessageDto response = sendRequest(KeypleMessageDto.Action.TRANSMIT, body);

    // Extract the response
    return KeypleJsonParser.getParser().fromJson(response.getBody(), SeResponse.class);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public boolean isSePresent() {

    // Send the message as a request
    KeypleMessageDto response = sendRequest(KeypleMessageDto.Action.IS_SE_PRESENT, null);

    // Extract the response
    return KeypleJsonParser.getParser().fromJson(response.getBody(), Boolean.class);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void releaseChannel() {
    // Send the message as a request even if no return is expected
    sendRequest(KeypleMessageDto.Action.RELEASE_CHANNEL, null);
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
    KeypleMessageDto response = sendRequest(KeypleMessageDto.Action.IS_READER_CONTACTLESS, null);

    // Extract the response
    return KeypleJsonParser.getParser().fromJson(response.getBody(), Boolean.class);
  }

  /**
   * (private)<br>
   * <li>Build the message using the provided action and body.
   * <li>Send it as a request.
   * <li>If an error is contained in the response, then throw an exception.
   *
   * @param action The action (must be not null).
   * @param body The body (optional).
   * @return a not null reference.
   */
  protected KeypleMessageDto sendRequest(KeypleMessageDto.Action action, JsonObject body) {

    // Build the message
    KeypleMessageDto message =
        new KeypleMessageDto() //
            .setSessionId(sessionId != null ? sessionId : UUID.randomUUID().toString()) //
            .setAction(action.name()) //
            .setVirtualReaderName(getName()) //
            .setNativeReaderName(nativeReaderName) //
            .setClientNodeId(clientNodeId) //
            .setBody(body != null ? body.toString() : null);

    // Send the message as a request
    KeypleMessageDto response = node.sendRequest(message);

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
  private void checkError(KeypleMessageDto message) {
    if (message.getAction().equals(KeypleMessageDto.Action.ERROR.name())) {
      BodyError body = KeypleJsonParser.getParser().fromJson(message.getBody(), BodyError.class);
      throw body.getException();
    }
  }

  /**
   * (package-private)<br>
   * Gets the current session id
   *
   * @return a nullable value.
   */
  String getSessionId() {
    return sessionId;
  }

  /**
   * (package-private)<br>
   * Gets the current client id
   *
   * @return a nullable value.
   */
  String getClientNodeId() {
    return clientNodeId;
  }
}
