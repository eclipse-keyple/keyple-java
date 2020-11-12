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

import java.util.UUID;
import org.eclipse.keyple.core.util.json.BodyError;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remote.*;
import org.eclipse.keyple.plugin.remote.spi.AsyncEndpointClient;
import org.eclipse.keyple.plugin.remote.spi.AsyncEndpointServer;
import org.eclipse.keyple.plugin.remote.spi.SyncEndpointClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (package-private)<br>
 * Abstract Message Handler.
 *
 * @since 1.0
 */
abstract class AbstractMessageHandler {

  private static final Logger logger = LoggerFactory.getLogger(AbstractMessageHandler.class);

  /**
   * (protected)<br>
   * The bounded node.
   */
  protected AbstractNode node;

  /**
   * (protected)<br>
   * Constructor.
   */
  protected AbstractMessageHandler() {}

  /**
   * (protected)<br>
   * This method processes an incoming message.<br>
   * It should be called by a node following the reception of a {@link MessageDto}.
   *
   * @param msg The message to process.
   */
  protected abstract void onMessage(MessageDto msg);

  /**
   * This method builds and bind a {@link AsyncNodeClient} with the handler.<br>
   * It must be called by the factory during the initialization phase.
   *
   * @param endpoint The {@link AsyncEndpointClient} endpoint.
   * @param timeoutInSecond Time to wait for the server to transmit a request.
   * @since 1.0
   */
  public void bindAsyncNodeClient(AsyncEndpointClient endpoint, int timeoutInSecond) {
    node = new AsyncNodeClientImpl(this, endpoint, timeoutInSecond);
  }

  /**
   * This method builds and bind a {@link AsyncNodeServer} with the handler.<br>
   * It must be called by the factory during the initialization phase.
   *
   * @param endpoint The {@link AsyncEndpointServer} endpoint.
   * @since 1.0
   */
  public void bindAsyncNodeServer(AsyncEndpointServer endpoint) {
    node = new AsyncNodeServerImpl(this, endpoint, 20);
  }

  /**
   * This method builds and bind a {@link SyncNodeClient} with the handler.<br>
   * It must be called by the factory during the initialization phase.
   *
   * @param endpoint The {@link SyncEndpointClient} endpoint.
   * @param pluginObservationStrategy The {@link ServerPushEventStrategy} associated to the plugin
   *     (null if observation is not activated).
   * @param readerObservationStrategy The {@link ServerPushEventStrategy} associated to the reader
   *     (null if observation is not activated).
   * @since 1.0
   */
  public void bindSyncNodeClient(
      SyncEndpointClient endpoint,
      ServerPushEventStrategy pluginObservationStrategy,
      ServerPushEventStrategy readerObservationStrategy) {
    node =
        new SyncNodeClientImpl(
            this, endpoint, pluginObservationStrategy, readerObservationStrategy);
  }

  /**
   * This method builds and bind a {@link SyncNodeServer} with the handler.<br>
   * It must be called by the factory during the initialization phase.
   *
   * @since 1.0
   */
  public void bindSyncNodeServer() {
    node = new SyncNodeServerImpl(this, 20);
  }

  /**
   * Gets the associated node.
   *
   * @return a not null reference.
   * @since 1.0
   */
  public AbstractNode getNode() {
    return node;
  }

  /**
   * (protected)<br>
   * If message contains an error, throws the embedded exception.
   *
   * @param message not null instance
   */
  protected void checkError(MessageDto message) {
    if (message.getAction().equals(MessageDto.Action.ERROR.name())) {
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
