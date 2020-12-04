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
import org.eclipse.keyple.core.util.json.KeypleGsonParser;
import org.eclipse.keyple.plugin.remote.*;
import org.eclipse.keyple.plugin.remote.spi.AsyncEndpointClient;
import org.eclipse.keyple.plugin.remote.spi.AsyncEndpointServer;
import org.eclipse.keyple.plugin.remote.spi.SyncEndpointClient;

/**
 * (package-private)<br>
 * Abstract Message Handler.
 *
 * @since 1.0
 */
abstract class AbstractMessageHandler {

  /**
   * (private-private)<br>
   * The bounded node.
   *
   * @since 1.0
   */
  AbstractNode node;

  /**
   * (package-private)<br>
   *
   * @since 1.0
   */
  AbstractMessageHandler() {}

  /**
   * (package-private)<br>
   * Processes an incoming message.<br>
   * It should be invoked by a node following the reception of a {@link MessageDto}.
   *
   * @param msg The message to process.
   * @since 1.0
   */
  abstract void onMessage(MessageDto msg);

  /**
   * (package-private)<br>
   * Builds and bind a {@link AsyncNodeClient} with the handler.<br>
   * It must be invoked by the factory during the initialization phase.
   *
   * @param endpoint The {@link AsyncEndpointClient} endpoint.
   * @param timeoutInSecond Time to wait for the server to transmit a request.
   * @since 1.0
   */
  void bindAsyncNodeClient(AsyncEndpointClient endpoint, int timeoutInSecond) {
    node = new AsyncNodeClientImpl(this, endpoint, timeoutInSecond);
  }

  /**
   * (package-private)<br>
   * Builds and bind a {@link AsyncNodeServer} with the handler.<br>
   * It must be invoked by the factory during the initialization phase.
   *
   * @param endpoint The {@link AsyncEndpointServer} endpoint.
   * @since 1.0
   */
  void bindAsyncNodeServer(AsyncEndpointServer endpoint) {
    node = new AsyncNodeServerImpl(this, endpoint, 20);
  }

  /**
   * (package-private)<br>
   * Builds and bind a {@link SyncNodeClient} with the handler.<br>
   * It must be invoked by the factory during the initialization phase.
   *
   * @since 1.0
   * @param endpoint The {@link SyncEndpointClient} endpoint.
   * @param pluginObservationStrategy The {@link ServerPushEventStrategy} associated to the plugin
   *     (null if observation is not activated).
   * @param readerObservationStrategy The {@link ServerPushEventStrategy} associated to the reader
   *     (null if observation is not activated).
   */
  void bindSyncNodeClient(
      SyncEndpointClient endpoint,
      ServerPushEventStrategy pluginObservationStrategy,
      ServerPushEventStrategy readerObservationStrategy) {
    node =
        new SyncNodeClientImpl(
            this, endpoint, pluginObservationStrategy, readerObservationStrategy);
  }

  /**
   * (package-private)<br>
   * Builds and bind a {@link SyncNodeServer} with the handler.<br>
   * It must be invoked by the factory during the initialization phase.
   *
   * @since 1.0
   */
  void bindSyncNodeServer() {
    node = new SyncNodeServerImpl(this, 20);
  }

  /**
   * (package-private)<br>
   * If message contains an error, throws the embedded exception.
   *
   * @param message not null instance
   * @since 1.0
   */
  void checkError(MessageDto message) {
    if (message.getAction().equals(MessageDto.Action.ERROR.name())) {
      BodyError body = KeypleGsonParser.getParser().fromJson(message.getBody(), BodyError.class);
      throw body.getException();
    }
  }

  /**
   * (package-private)<br>
   * Generate a unique session Id.
   *
   * @return A not empty value.
   * @since 1.0
   */
  String generateSessionId() {
    return UUID.randomUUID().toString();
  }
}
