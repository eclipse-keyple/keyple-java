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
package org.eclipse.keyple.plugin.remote.integration.common.endpoint.service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.keyple.core.service.exception.KeypleRuntimeException;
import org.eclipse.keyple.core.util.NamedThreadFactory;
import org.eclipse.keyple.plugin.remote.KeypleClientSync;
import org.eclipse.keyple.plugin.remote.KeypleMessageDto;
import org.eclipse.keyple.plugin.remote.KeypleServerSyncNode;
import org.eclipse.keyple.plugin.remote.RemoteServerPlugin;
import org.eclipse.keyple.plugin.remote.integration.common.endpoint.StubNetworkConnectionException;
import org.eclipse.keyple.plugin.remote.integration.common.util.JacksonParser;
import org.eclipse.keyple.plugin.remote.impl.RemoteServerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stub implementation of a {@link KeypleClientSync} to test {@link
 * RemoteServerPlugin}. It simulates synchronous invocation
 * to a remote server.
 */
public class StubSyncClientEndpoint implements KeypleClientSync {

  private static final Logger logger = LoggerFactory.getLogger(StubSyncClientEndpoint.class);
  static final ExecutorService taskPool =
      Executors.newCachedThreadPool(new NamedThreadFactory("syncPool"));;
  private final Boolean simulateConnectionError;
  int messageSent = 0;

  public StubSyncClientEndpoint(Boolean simulateConnectionError) {
    this.simulateConnectionError = simulateConnectionError;
  }

  @Override
  public List<KeypleMessageDto> sendRequest(KeypleMessageDto msg) {
    if (messageSent++ == 2 && simulateConnectionError) {
      throw new StubNetworkConnectionException("Simulate a host unreacheable error");
    }

    final String responsesJson;
    // serialize request
    final String request = JacksonParser.toJson(msg);
    logger.trace("Sending request data to server : {}", request);

    try {
      responsesJson = taskPool.submit(sendData(request)).get();

      List<KeypleMessageDto> responses = JacksonParser.fromJsonList(responsesJson);

      return responses;
    } catch (InterruptedException e) {
      throw new KeypleRuntimeException("Impossible to process incoming message", e);
    } catch (ExecutionException e) {
      throw new KeypleRuntimeException("Impossible to process incoming message", e);
    }
  }

  /**
   * @param data json serialized (keyple message dto)
   * @return json serialized data (list of keyple dto)
   */
  Callable<String> sendData(final String data) {
    return new Callable<String>() {
      @Override
      public String call() throws Exception {
        KeypleServerSyncNode serverSyncNode;

        // Send the dto to the sync node
        List<KeypleMessageDto> responses =
            RemoteServerUtils.getSyncNode().onRequest(JacksonParser.fromJson(data));

        return JacksonParser.toJson(responses);
      }
    };
  }
}
