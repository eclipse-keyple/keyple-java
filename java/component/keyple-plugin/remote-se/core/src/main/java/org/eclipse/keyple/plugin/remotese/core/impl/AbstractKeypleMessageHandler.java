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
package org.eclipse.keyple.plugin.remotese.core.impl;

import java.util.UUID;
import org.eclipse.keyple.core.util.json.BodyError;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remotese.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Keyple Message Handler.
 *
 * <p>This is an internal class an must not be used by the user.
 *
 * @since 1.0
 */
public abstract class AbstractKeypleMessageHandler {

  private static final Logger logger = LoggerFactory.getLogger(AbstractKeypleMessageHandler.class);

  /**
   * (protected)<br>
   * The bounded node.
   */
  protected AbstractKeypleNode node;

  /**
   * (protected)<br>
   * Constructor.
   */
  protected AbstractKeypleMessageHandler() {}

  /**
   * (protected)<br>
   * This method processes an incoming message.<br>
   * It should be called by a node following the reception of a {@link KeypleMessageDto}.
   *
   * @param msg The message to process.
   */
  protected abstract void onMessage(KeypleMessageDto msg);

  /**
   * This method builds and bind a {@link KeypleClientAsyncNode} with the handler.<br>
   * It must be called by the factory during the initialization phase.
   *
   * @param endpoint The {@link KeypleClientAsync} endpoint.
   * @param timeoutInSecond
   * @since 1.0
   */
  public void bindClientAsyncNode(KeypleClientAsync endpoint, int timeoutInSecond) {
    node = new KeypleClientAsyncNodeImpl(this, endpoint, timeoutInSecond);
  }

  /**
   * This method builds and bind a {@link KeypleServerAsyncNode} with the handler.<br>
   * It must be called by the factory during the initialization phase.
   *
   * @param endpoint The {@link KeypleServerAsync} endpoint.
   * @since 1.0
   */
  public void bindServerAsyncNode(KeypleServerAsync endpoint) {
    node = new KeypleServerAsyncNodeImpl(this, endpoint, 20);
  }

  /**
   * This method builds and bind a {@link KeypleClientSyncNode} with the handler.<br>
   * It must be called by the factory during the initialization phase.
   *
   * @param endpoint The {@link KeypleClientSync} endpoint.
   * @param pluginObservationStrategy The {@link ServerPushEventStrategy} associated to the plugin
   *     (null if observation is not activated).
   * @param readerObservationStrategy The {@link ServerPushEventStrategy} associated to the reader
   *     (null if observation is not activated).
   * @since 1.0
   */
  public void bindClientSyncNode(
      KeypleClientSync endpoint,
      ServerPushEventStrategy pluginObservationStrategy,
      ServerPushEventStrategy readerObservationStrategy) {
    node =
        new KeypleClientSyncNodeImpl(
            this, endpoint, pluginObservationStrategy, readerObservationStrategy);
  }

  /**
   * This method builds and bind a {@link KeypleServerSyncNode} with the handler.<br>
   * It must be called by the factory during the initialization phase.
   *
   * @since 1.0
   */
  public void bindServerSyncNode() {
    node = new KeypleServerSyncNodeImpl(this, 20);
  }

  /**
   * Gets the associated node.
   *
   * @return a not null reference.
   * @since 1.0
   */
  public AbstractKeypleNode getNode() {
    return node;
  }

  /**
   * (protected)<br>
   * If message contains an error, throws the embedded exception.
   *
   * @param message not null instance
   */
  protected void checkError(KeypleMessageDto message) {
    if (message.getAction().equals(KeypleMessageDto.Action.ERROR.name())) {
      BodyError body = KeypleJsonParser.getParser().fromJson(message.getBody(), BodyError.class);
      throw body.getException();
    }
  }

  /**
   * (protected)<br>
   * Generate a unique session Id.
   *
   * @return A not empty value.
   */
  protected String generateSessionId() {
    return UUID.randomUUID().toString();
  }
}
